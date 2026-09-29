import { ClientMessage, ServerMessage } from './protocol';

interface SocketLike {
    send(data: string): void;
    close(): void;
}

export class NetClient {
    onMessage: ((message: ServerMessage) => void) | null = null;
    onClose: (() => void) | null = null;
    private socket: SocketLike | null = null;

    connect(url: string): Promise<void> {
        this.close();
        return new Promise((resolve, reject) => {
            let settled = false;
            const ok = () => {
                if (!settled) {
                    settled = true;
                    resolve();
                }
            };
            const fail = (message: string) => {
                if (!settled) {
                    settled = true;
                    reject(new Error(message));
                }
            };
            if (typeof wx !== 'undefined' && typeof wx.connectSocket === 'function') {
                const task = wx.connectSocket({ url });
                task.onOpen(ok);
                task.onMessage((res) => this.emit(res.data));
                task.onError((error) => fail(error?.errMsg || '连接失败'));
                task.onClose(() => this.onClose?.());
                this.socket = {
                    send: (data) => task.send({ data }),
                    close: () => task.close({}),
                };
                return;
            }
            const ws = new WebSocket(url);
            ws.onopen = ok;
            ws.onmessage = (event) => this.emit(event.data);
            ws.onerror = () => fail('连接失败');
            ws.onclose = () => this.onClose?.();
            this.socket = {
                send: (data) => ws.send(data),
                close: () => ws.close(),
            };
        });
    }

    send(message: ClientMessage): void {
        this.socket?.send(JSON.stringify(message));
    }

    close(): void {
        this.socket?.close();
        this.socket = null;
    }

    private emit(data: string | ArrayBuffer): void {
        const text = typeof data === 'string' ? data : '';
        if (!text) {
            return;
        }
        try {
            this.onMessage?.(JSON.parse(text) as ServerMessage);
        } catch (error) {
            this.onMessage?.({ type: 'error', message: '消息无法解析' });
        }
    }
}
