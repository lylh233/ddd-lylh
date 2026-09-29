import { randomBytes, randomInt } from 'node:crypto';
import { WebSocket, WebSocketServer } from 'ws';
import { GameEngine } from '../../assets/scripts/game/GameEngine.ts';
import { defaultRoles, isPlayerCount, shuffleRoles } from '../../assets/scripts/game/presets.ts';
import { isRoleId, RoleId } from '../../assets/scripts/game/types.ts';
import { ClientMessage, RoomState, ServerMessage } from '../../assets/scripts/net/protocol.ts';

interface Seat {
    name: string;
    token: string;
    socket: WebSocket | null;
}

interface Room {
    id: string;
    playerCount: number;
    roles: RoleId[];
    seats: (Seat | null)[];
    engine: GameEngine | null;
    stage: 'lobby' | 'playing' | 'settlement';
}

interface Client {
    socket: WebSocket;
    roomId?: string;
    seat?: number;
}

const rooms = new Map<string, Room>();
const clients = new WeakMap<WebSocket, Client>();
const port = Number(process.env.PORT || 8787);

export const server = new WebSocketServer({ port });
server.on('connection', (socket) => {
    const client: Client = { socket };
    clients.set(socket, client);
    socket.on('message', (data) => {
        try {
            const message = JSON.parse(data.toString()) as ClientMessage;
            handle(client, message);
        } catch (error) {
            send(socket, { type: 'error', message: '消息无法解析' });
        }
    });
    socket.on('close', () => leave(client, false));
});

const timer = setInterval(() => {
    rooms.forEach((room) => {
        if (!room.engine || room.engine.winner) {
            return;
        }
        const before = room.engine.signature();
        room.engine.tick(200);
        if (room.engine.signature() !== before) {
            publishView(room);
        }
    });
}, 200);
timer.unref();

server.on('listening', () => {
    console.log(`werewolf server listening on ws://127.0.0.1:${port}`);
});

function handle(client: Client, message: ClientMessage): void {
    if (message.type === 'create_room') {
        createRoom(client, message.name);
        return;
    }
    if (message.type === 'join_room') {
        joinRoom(client, message.roomId, message.name, message.token);
        return;
    }
    const room = client.roomId ? rooms.get(client.roomId) : undefined;
    if (!room || !client.seat) {
        send(client.socket, { type: 'error', message: '还没有进入房间' });
        return;
    }
    if (message.type === 'set_config') {
        setConfig(client, room, message.playerCount, message.roles);
        return;
    }
    if (message.type === 'start_game') {
        startGame(client, room);
        return;
    }
    if (message.type === 'action') {
        if (!room.engine || room.stage === 'lobby') {
            send(client.socket, { type: 'error', message: '对局还没开始' });
            return;
        }
        const result = room.engine.act(client.seat, message.action);
        if (!result.ok) {
            send(client.socket, { type: 'error', message: result.error || '操作失败' });
            return;
        }
        publishView(room);
        return;
    }
    if (message.type === 'leave') {
        leave(client, true);
    }
}

function createRoom(client: Client, name: string): void {
    const cleaned = cleanName(name);
    if (!cleaned) {
        send(client.socket, { type: 'error', message: '请输入玩家名称' });
        return;
    }
    leave(client, true);
    const room: Room = {
        id: newRoomId(),
        playerCount: 8,
        roles: defaultRoles(8),
        seats: Array.from({ length: 8 }, () => null),
        engine: null,
        stage: 'lobby',
    };
    const token = randomBytes(16).toString('hex');
    room.seats[0] = { name: cleaned, token, socket: client.socket };
    rooms.set(room.id, room);
    client.roomId = room.id;
    client.seat = 1;
    send(client.socket, { type: 'hello', roomId: room.id, seat: 1, token });
    publishRoom(room);
}

function joinRoom(client: Client, roomId: string, name: string, token?: string): void {
    const cleaned = cleanName(name);
    if (!cleaned) {
        send(client.socket, { type: 'error', message: '请输入玩家名称' });
        return;
    }
    const room = rooms.get(normalizeRoomId(roomId));
    if (!room) {
        send(client.socket, { type: 'error', message: '房间不存在' });
        return;
    }
    if (token) {
        const index = room.seats.findIndex((seat) => seat?.token === token);
        const seat = index >= 0 ? room.seats[index] : null;
        if (seat && !seatTaken(seat, client.socket)) {
            leave(client, true);
            if (seat.socket && seat.socket !== client.socket) {
                const previous = clients.get(seat.socket);
                if (previous) {
                    previous.roomId = undefined;
                    previous.seat = undefined;
                }
            }
            seat.socket = client.socket;
            seat.name = cleaned;
            client.roomId = room.id;
            client.seat = index + 1;
            send(client.socket, { type: 'hello', roomId: room.id, seat: client.seat, token: seat.token });
            publishRoom(room);
            if (room.engine) {
                send(client.socket, { type: 'game_view', view: room.engine.view(client.seat) });
            }
            return;
        }
    }
    if (room.stage !== 'lobby') {
        send(client.socket, { type: 'error', message: '对局已经开始' });
        return;
    }
    const index = room.seats.findIndex((seat) => !seat);
    if (index < 0) {
        send(client.socket, { type: 'error', message: '房间已满' });
        return;
    }
    leave(client, true);
    const nextToken = randomBytes(16).toString('hex');
    room.seats[index] = { name: cleaned, token: nextToken, socket: client.socket };
    client.roomId = room.id;
    client.seat = index + 1;
    send(client.socket, { type: 'hello', roomId: room.id, seat: client.seat, token: nextToken });
    publishRoom(room);
}

function setConfig(client: Client, room: Room, playerCount: number, roles: RoleId[]): void {
    if (client.seat !== 1 || room.stage !== 'lobby') {
        send(client.socket, { type: 'error', message: '只有房主能在开局前修改配置' });
        return;
    }
    if (!isPlayerCount(playerCount) || !Array.isArray(roles) || roles.length > playerCount || roles.some((role) => !isRoleId(role))) {
        send(client.socket, { type: 'error', message: '身份配置无效' });
        return;
    }
    const occupied = room.seats.some((seat, index) => !!seat && index >= playerCount);
    if (occupied) {
        send(client.socket, { type: 'error', message: '请先减少已入座的玩家' });
        return;
    }
    const next: (Seat | null)[] = Array.from({ length: playerCount }, (_, index) => room.seats[index] || null);
    room.playerCount = playerCount;
    room.roles = roles.slice();
    room.seats = next;
    publishRoom(room);
}

function startGame(client: Client, room: Room): void {
    if (client.seat !== 1) {
        send(client.socket, { type: 'error', message: '只有房主可以开始' });
        return;
    }
    if (room.seats.some((seat) => !seat)) {
        send(client.socket, { type: 'error', message: '人未坐满，不能开始' });
        return;
    }
    if (room.roles.length !== room.playerCount) {
        send(client.socket, { type: 'error', message: '身份数量需要等于入座人数' });
        return;
    }
    room.engine = new GameEngine();
    room.engine.start(room.seats.map((seat) => seat!.name), shuffleRoles(room.roles));
    room.stage = 'playing';
    publishRoom(room);
    publishView(room);
}

function leave(client: Client, notify: boolean): void {
    if (!client.roomId || !client.seat) {
        return;
    }
    const room = rooms.get(client.roomId);
    const seatIndex = client.seat - 1;
    client.roomId = undefined;
    client.seat = undefined;
    if (!room) {
        return;
    }
    const seat = room.seats[seatIndex];
    if (room.stage === 'lobby') {
        if (!seat || seat.socket !== client.socket) {
            return;
        }
        if (seatIndex === 0) {
            room.seats.forEach((item) => {
                if (item?.socket && item.socket !== client.socket) {
                    send(item.socket, { type: 'error', message: '房主已离开' });
                    const other = clients.get(item.socket);
                    if (other) {
                        other.roomId = undefined;
                        other.seat = undefined;
                    }
                }
            });
            rooms.delete(room.id);
            return;
        }
        room.seats[seatIndex] = null;
        if (notify) {
            publishRoom(room);
        }
        return;
    }
    if (seat && seat.socket === client.socket) {
        seat.socket = null;
    }
}

function publishRoom(room: Room): void {
    room.seats.forEach((seat, index) => {
        if (!seat?.socket) {
            return;
        }
        send(seat.socket, { type: 'room_state', room: roomState(room, index + 1) });
    });
}

function publishView(room: Room): void {
    if (!room.engine) {
        return;
    }
    if (room.engine.winner) {
        room.stage = 'settlement';
    }
    room.seats.forEach((seat, index) => {
        if (!seat?.socket) {
            return;
        }
        send(seat.socket, { type: 'game_view', view: room.engine!.view(index + 1) });
    });
}

function roomState(room: Room, selfSeat: number): RoomState {
    return {
        roomId: room.id,
        hostSeat: 1,
        playerCount: room.playerCount,
        roles: room.roles.slice(),
        stage: room.stage,
        selfSeat,
        seats: room.seats.map((seat, index) => ({
            seat: index + 1,
            name: seat?.name || '',
            filled: !!seat,
        })),
    };
}

function send(socket: WebSocket, message: ServerMessage): void {
    if (socket.readyState === WebSocket.OPEN) {
        socket.send(JSON.stringify(message));
    }
}

function cleanName(name: string): string {
    return (name || '').trim().slice(0, 8);
}

function normalizeRoomId(roomId: string): string {
    return String(roomId ?? '').replace(/\D/g, '');
}

function seatTaken(seat: Seat, socket: WebSocket): boolean {
    return !!seat.socket && seat.socket.readyState === WebSocket.OPEN && seat.socket !== socket;
}

function newRoomId(): string {
    for (let index = 0; index < 20; index += 1) {
        const id = String(randomInt(100000, 1000000));
        if (!rooms.has(id)) {
            return id;
        }
    }
    return String(Date.now()).slice(-6);
}
