import assert from 'node:assert/strict';
import { after, test } from 'node:test';
import { WebSocket } from 'ws';
import { server } from './src/index.ts';

const port = Number(process.env.PORT || 8787);

after(async () => {
    await new Promise<void>((resolve) => server.close(() => resolve()));
});

interface Msg {
    type: string;
    roomId?: string;
    seat?: number;
    token?: string;
    message?: string;
    room?: {
        roomId: string;
        selfSeat: number;
        seats: { seat: number; name: string; filled: boolean }[];
    };
}

function openClient(): Promise<{ socket: WebSocket; messages: Msg[] }> {
    return new Promise((resolve, reject) => {
        const socket = new WebSocket(`ws://127.0.0.1:${port}`);
        const messages: Msg[] = [];
        socket.on('message', (data) => messages.push(JSON.parse(data.toString()) as Msg));
        socket.on('open', () => resolve({ socket, messages }));
        socket.on('error', reject);
    });
}

async function waitFor(messages: Msg[], match: (message: Msg) => boolean): Promise<Msg> {
    const start = Date.now();
    while (Date.now() - start < 2000) {
        const found = messages.find(match);
        if (found) {
            return found;
        }
        await new Promise((resolve) => setTimeout(resolve, 20));
    }
    throw new Error(`超时 ${JSON.stringify(messages)}`);
}

test('同一个房间号会进同一个房间，不会挤掉在线玩家', async () => {
    const host = await openClient();
    host.socket.send(JSON.stringify({ type: 'create_room', name: '浩' }));
    const hello = await waitFor(host.messages, (message) => message.type === 'hello');
    const guest = await openClient();
    guest.socket.send(JSON.stringify({
        type: 'join_room',
        roomId: `房间号${hello.roomId}`,
        name: '明',
        token: hello.token,
    }));
    const guestHello = await waitFor(guest.messages, (message) => message.type === 'hello');
    assert.equal(guestHello.roomId, hello.roomId);
    assert.equal(guestHello.seat, 2);
    const filled = (message: Msg) => message.type === 'room_state' && message.room!.seats.filter((seat) => seat.filled).length === 2;
    const hostRoom = await waitFor(host.messages, filled);
    const guestRoom = await waitFor(guest.messages, filled);
    assert.equal(hostRoom.room!.roomId, guestRoom.room!.roomId);
    assert.equal(hostRoom.room!.selfSeat, 1);
    assert.equal(guestRoom.room!.selfSeat, 2);
    assert.deepEqual(hostRoom.room!.seats.filter((seat) => seat.filled).map((seat) => seat.name), ['浩', '明']);
    host.socket.close();
    guest.socket.close();
});

test('没有玩家名称不能创建房间', async () => {
    const client = await openClient();
    client.socket.send(JSON.stringify({ type: 'create_room', name: '   ' }));
    const error = await waitFor(client.messages, (message) => message.type === 'error');
    assert.equal(error.message, '请输入玩家名称');
    client.socket.close();
});
