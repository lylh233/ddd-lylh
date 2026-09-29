import { _decorator, Canvas, Component, director, Director, Layers, Node, sys, UITransform, view, Widget } from 'cc';
import { BackgroundHelper } from '../adapt/BackgroundHelper';
import { LayoutHelper } from '../adapt/LayoutHelper';
import { ScreenAdapter } from '../adapt/ScreenAdapter';
import { decideAi } from '../game/ai';
import { GameEngine } from '../game/GameEngine';
import { defaultRoles, isPlayerCount, shuffleRoles } from '../game/presets';
import { GameAction, GameView, RoleId, roleInfo } from '../game/types';
import { WS_URL } from '../net/GameConfig';
import { NetClient } from '../net/NetClient';
import { RoomState, ServerMessage } from '../net/protocol';
import { CreateRoomPage } from '../ui/CreateRoomPage';
import { GamePage } from '../ui/GamePage';
import { HomePage } from '../ui/HomePage';
import { createNode, ModalHost } from '../ui/UiFactory';
import { RoomPage } from '../ui/RoomPage';
import { SettlementPage } from '../ui/SettlementPage';
import { Theme } from '../ui/theme';

const { ccclass } = _decorator;

type ScreenName = 'home' | 'create' | 'room' | 'game' | 'end';

@ccclass('GameApp')
export class GameApp extends Component {
    private content!: Node;
    private modal!: ModalHost;
    private page: Node | null = null;
    private engine: GameEngine | null = null;
    private net: NetClient | null = null;
    private mode: 'local' | 'online' = 'local';
    private screen: ScreenName = 'home';
    private playerName = '';
    private playerCount = 8;
    private roles: RoleId[] = defaultRoles(8);
    private room: RoomState | null = null;
    private humanSeat = 1;
    private shown = '';
    private currentView: GameView | null = null;
    private logOpen = false;
    private logDay = 1;
    private noticeId = 0;
    private expectClose = false;
    private width = 750;
    private height = 1334;

    onLoad(): void {
        const globals = director.getScene()?.globals as { postSettings?: { toneMappingType: number } } | null;
        if (globals?.postSettings) {
            globals.postSettings.toneMappingType = 1;
        }
        this.node.layer = Layers.Enum.UI_2D;
        const canvas = this.getComponent(Canvas);
        if (canvas) {
            canvas.alignCanvasWithScreen = true;
            if (canvas.cameraComponent) {
                canvas.cameraComponent.clearColor = Theme.bg;
            }
        }
        const widget = this.getComponent(Widget) || this.addComponent(Widget);
        LayoutHelper.attachWidget(this.node, { top: 0, bottom: 0, left: 0, right: 0 });
        widget.updateAlignment();
        const adapter = this.getComponent(ScreenAdapter) || this.addComponent(ScreenAdapter);
        adapter.onChange(() => this.layout());
        const background = createNode(this.node, 'background', 750, 1334);
        BackgroundHelper.setBackground(background);
        this.content = createNode(this.node, 'content', 750, 1334);
        this.modal = new ModalHost(this.content, 750, 1334);
        this.playerName = '';
        this.layout();
        this.scheduleOnce(() => this.layout(), 0);
    }

    update(dt: number): void {
        if (this.mode !== 'local' || !this.engine || this.screen === 'home' || this.screen === 'room' || this.screen === 'create') {
            return;
        }
        if (!this.engine.winner) {
            this.engine.tick(dt * 1000);
        }
        const signature = this.engine.signature();
        if (signature !== this.shown) {
            this.shown = signature;
            this.renderLocal();
        }
    }

    private layout(): void {
        const visible = view.getVisibleSize();
        this.width = Math.min(ScreenAdapter.DESIGN_WIDTH, visible.width || ScreenAdapter.DESIGN_WIDTH);
        this.height = visible.height || ScreenAdapter.DESIGN_HEIGHT;
        this.content.getComponent(UITransform)!.setContentSize(this.width, this.height);
        this.modal.resize(this.width, this.height);
        this.render();
    }

    private render(): void {
        if (this.screen === 'home') {
            this.showHome();
        } else if (this.screen === 'create') {
            this.showCreate();
        } else if (this.screen === 'room') {
            this.showRoom();
        } else if (this.screen === 'end' && this.currentView) {
            this.showSettlement(this.currentView);
        } else if (this.screen === 'game' && this.currentView) {
            this.showGame(this.currentView, true);
        }
    }

    private showHome(): void {
        this.screen = 'home';
        this.mount(new HomePage(this.content, this.width, this.height, this.playerName, (name) => {
            if (name) {
                this.playerName = name;
            }
        }, () => this.modal.alert('请输入玩家名称'), () => {
            this.mode = 'local';
            this.closeNet();
            this.playerCount = 8;
            this.roles = defaultRoles(8);
            this.room = null;
            this.showRoom();
        }, () => this.showCreate()).node);
    }

    private showCreate(): void {
        this.screen = 'create';
        this.mount(new CreateRoomPage(this.content, this.width, this.height, () => {
            if (!this.requireName()) {
                return;
            }
            this.connect().then(() => this.net?.send({ type: 'create_room', name: this.playerName })).catch((error: Error) => {
                this.modal.alert(error.message || '连接失败');
            });
        }, (roomId) => {
            const digits = roomId.replace(/\D/g, '');
            if (digits.length !== 6) {
                this.modal.alert('请输入房间号');
                return;
            }
            if (!this.requireName()) {
                return;
            }
            const token = this.readStorage('lrs-room') === digits ? this.readStorage('lrs-token') : '';
            this.connect().then(() => this.net?.send({
                type: 'join_room',
                roomId: digits,
                name: this.playerName,
                token: token || undefined,
            })).catch((error: Error) => this.modal.alert(error.message || '连接失败'));
        }, () => {
            this.closeNet();
            this.showHome();
        }).node);
    }

    private showRoom(): void {
        this.screen = 'room';
        const online = this.mode === 'online' && !!this.room;
        const seats = online && this.room
            ? this.room.seats
            : Array.from({ length: this.playerCount }, (_, index) => ({
                seat: index + 1,
                name: index === 0 ? this.playerName : `玩家${index + 1}`,
                filled: true,
            }));
        const canEdit = !online || this.room!.selfSeat === this.room!.hostSeat;
        this.mount(new RoomPage(this.content, this.width, this.height, {
            roomId: this.room?.roomId || '',
            playerCount: online && this.room ? this.room.playerCount : this.playerCount,
            roles: online && this.room ? this.room.roles : this.roles,
            seats,
            canEdit,
            online,
        }, {
            onCount: (count) => this.changeCount(count),
            onRoles: (roles) => this.changeRoles(roles),
            onStart: () => this.startMatch(),
            onBack: () => {
                if (online) {
                    this.net?.send({ type: 'leave' });
                    this.closeNet();
                    this.room = null;
                    this.showCreate();
                } else {
                    this.showHome();
                }
            },
        }).node);
    }

    private showGame(gameView: GameView, keepModal = false): void {
        const changed = this.phaseChanged(gameView);
        this.screen = 'game';
        this.currentView = gameView;
        if (!keepModal && changed) {
            this.modal.clear();
        }
        const adapter = this.getComponent(ScreenAdapter);
        const safeTop = adapter ? adapter.getTopSafePadding() : 0;
        const safeBottom = adapter ? adapter.getSafeArea().bottom : 0;
        this.mount(new GamePage(this.content, this.width, this.height, gameView, safeTop, safeBottom, this.logOpen, this.logDay, {
            onAction: (id) => this.onAction(id),
            onSelf: () => this.modal.alert(`${roleInfo(gameView.selfRole).name}\n${roleInfo(gameView.selfRole).desc}`),
            onToggleLog: () => {
                this.logOpen = !this.logOpen;
                this.logDay = gameView.day;
                this.showGame(gameView, true);
            },
            onLogDay: (day) => {
                this.logDay = day;
                this.showGame(gameView, true);
            },
        }).node);
        if (gameView.notice && gameView.notice.id !== this.noticeId) {
            this.noticeId = gameView.notice.id;
            this.modal.alert(gameView.notice.text);
        }
    }

    private showSettlement(gameView: GameView): void {
        this.screen = 'end';
        this.currentView = gameView;
        this.mount(new SettlementPage(this.content, this.width, this.height, gameView, this.logOpen, this.logDay, () => {
            this.engine = null;
            this.shown = '';
            if (this.mode === 'online') {
                this.net?.send({ type: 'leave' });
                this.closeNet();
            }
            this.room = null;
            this.showHome();
        }, () => {
            this.logOpen = !this.logOpen;
            this.logDay = gameView.day;
            this.showSettlement(gameView);
        }, (day) => {
            this.logDay = day;
            this.showSettlement(gameView);
        }).node);
    }

    private startMatch(): void {
        const count = this.room && this.mode === 'online' ? this.room.playerCount : this.playerCount;
        const roles = this.room && this.mode === 'online' ? this.room.roles : this.roles;
        if (roles.length !== count) {
            this.modal.alert('身份数量需要等于入座人数');
            return;
        }
        if (this.mode === 'online') {
            const filled = this.room?.seats.filter((seat) => seat.filled).length || 0;
            if (filled < count) {
                this.modal.alert('人未坐满，不能开始');
                return;
            }
            this.net?.send({ type: 'start_game' });
            return;
        }
        const names = [this.playerName];
        for (let seat = 2; seat <= count; seat += 1) {
            names.push(`玩家${seat}`);
        }
        this.engine = new GameEngine();
        this.engine.start(names, shuffleRoles(roles));
        this.humanSeat = 1;
        this.shown = '';
        this.logOpen = false;
        this.renderLocal();
    }

    private renderLocal(): void {
        if (!this.engine) {
            return;
        }
        const gameView = this.engine.view(this.humanSeat);
        this.shown = this.engine.signature();
        if (gameView.winner) {
            this.showSettlement(gameView);
            return;
        }
        this.showGame(gameView);
        this.queueAi();
    }

    private queueAi(): void {
        this.unschedule(this.runAi);
        this.scheduleOnce(this.runAi, 0.6);
    }

    private runAi = (): void => {
        if (this.mode !== 'local' || !this.engine || this.engine.winner) {
            return;
        }
        for (let round = 0; round < 8; round += 1) {
            let acted = false;
            for (let seat = 2; seat <= this.engine.players.length; seat += 1) {
                const action = decideAi(this.engine, seat);
                if (action && this.engine.act(seat, action).ok) {
                    acted = true;
                }
            }
            if (!acted) {
                break;
            }
        }
        const signature = this.engine.signature();
        if (signature !== this.shown) {
            this.shown = signature;
            this.renderLocal();
        }
    };

    private onAction(id: string): void {
        const gameView = this.currentView;
        if (!gameView) {
            return;
        }
        const button = gameView.actions.find((action) => action.id === id);
        const send = (action: GameAction) => {
            if (this.mode === 'online') {
                this.net?.send({ type: 'action', action });
                return;
            }
            if (!this.engine) {
                return;
            }
            const result = this.engine.act(this.humanSeat, action);
            if (!result.ok) {
                this.modal.alert(result.error || '操作失败');
                return;
            }
            this.renderLocal();
        };
        if (button?.options) {
            if (id === 'explode') {
                this.modal.pick('自爆', button.options, (target) => {
                    const text = target == null ? '确定自爆且不带走玩家？当天白天将结束。' : `确定自爆并淘汰${target}号？当天白天将结束。`;
                    this.modal.confirm(text, () => send({ type: 'explode', target }));
                });
                return;
            }
            this.modal.pick(button.label, button.options, (target) => send(actionWithTarget(id, target)));
            return;
        }
        const text = confirmText(id, gameView);
        if (text) {
            this.modal.confirm(text, () => send(actionWithoutTarget(id)));
            return;
        }
        send(actionWithoutTarget(id));
    }

    private changeCount(count: number): void {
        if (!isPlayerCount(count)) {
            return;
        }
        this.playerCount = count;
        this.roles = defaultRoles(count);
        if (this.mode === 'online') {
            this.net?.send({ type: 'set_config', playerCount: count, roles: this.roles });
        }
        this.showRoom();
    }

    private changeRoles(roles: RoleId[]): void {
        this.roles = roles;
        if (this.mode === 'online') {
            this.net?.send({ type: 'set_config', playerCount: this.playerCount, roles });
        }
        this.showRoom();
    }

    private requireName(): boolean {
        if (this.playerName.trim()) {
            return true;
        }
        this.modal.alert('请输入玩家名称');
        return false;
    }

    private connect(): Promise<void> {
        if (this.net) {
            return Promise.resolve();
        }
        const net = new NetClient();
        net.onMessage = (message) => this.onNet(message);
        net.onClose = () => {
            if (this.expectClose) {
                this.expectClose = false;
                return;
            }
            if (this.mode === 'online' && this.screen !== 'home') {
                this.modal.alert('连接已断开');
            }
        };
        this.mode = 'online';
        return net.connect(WS_URL).then(() => {
            this.net = net;
        });
    }

    private onNet(message: ServerMessage): void {
        if (message.type === 'hello') {
            this.humanSeat = message.seat;
            this.writeStorage('lrs-token', message.token);
            this.writeStorage('lrs-room', message.roomId);
            return;
        }
        if (message.type === 'error') {
            this.modal.alert(message.message);
            return;
        }
        if (message.type === 'room_state') {
            this.room = message.room;
            this.playerCount = message.room.playerCount;
            this.roles = message.room.roles.slice();
            this.humanSeat = message.room.selfSeat;
            if (message.room.stage === 'lobby') {
                this.showRoom();
            }
            return;
        }
        if (message.type === 'game_view') {
            this.humanSeat = message.view.selfSeat;
            if (message.view.winner) {
                this.showSettlement(message.view);
            } else {
                this.showGame(message.view);
            }
        }
    }

    private closeNet(): void {
        if (!this.net) {
            return;
        }
        this.expectClose = true;
        this.net.close();
        this.net = null;
    }

    private mount(node: Node): void {
        if (this.page && this.page !== node) {
            this.page.destroy();
        }
        this.page = node;
        this.modal.bringToFront();
    }

    private phaseChanged(gameView: GameView): boolean {
        return !this.currentView || this.currentView.phase !== gameView.phase || this.currentView.prompt !== gameView.prompt;
    }

    private readStorage(key: string): string {
        try {
            return sys.localStorage.getItem(key) || '';
        } catch (error) {
            return '';
        }
    }

    private writeStorage(key: string, value: string): void {
        try {
            sys.localStorage.setItem(key, value);
        } catch (error) {
            // 预览环境可能没有本地存储。
        }
    }
}

function confirmText(id: string, view: GameView): string {
    if (id === 'campaign') {
        return '确定上警？';
    }
    if (id === 'step_down') {
        return '确定下警？';
    }
    if (id === 'start_speech') {
        return '确定开始发言？';
    }
    if (id === 'start_vote') {
        return '确定开始投票？';
    }
    if (id === 'antidote') {
        return '确定使用解药？';
    }
    if (id === 'witch_pass') {
        return '确定结束行动？';
    }
    if (id === 'reveal_idiot') {
        return '确定亮明身份？亮明后不会被放逐。';
    }
    if (id === 'accept_exile') {
        return '确定接受放逐？';
    }
    if (id === 'knife_confirm') {
        return view.agreedKnife == null ? '确定要空刀么？' : `确定要刀${view.agreedKnife}号么？`;
    }
    return '';
}

function actionWithTarget(id: string, target: number | null): GameAction {
    if (id === 'guard') {
        return { type: 'guard', target };
    }
    if (id === 'knife') {
        return { type: 'knife', target };
    }
    if (id === 'vote') {
        return { type: 'vote', target };
    }
    if (id === 'badge') {
        return { type: 'badge', target };
    }
    if (id === 'shoot') {
        return { type: 'shoot', target };
    }
    if (id === 'explode') {
        return { type: 'explode', target };
    }
    if (id === 'seer') {
        return { type: 'seer', target: target || 1 };
    }
    return { type: 'poison', target: target || 1 };
}

function actionWithoutTarget(id: string): GameAction {
    if (id === 'knife_confirm') {
        return { type: 'knife_confirm' };
    }
    if (id === 'antidote') {
        return { type: 'antidote' };
    }
    if (id === 'witch_pass') {
        return { type: 'witch_pass' };
    }
    if (id === 'campaign') {
        return { type: 'campaign' };
    }
    if (id === 'step_down') {
        return { type: 'step_down' };
    }
    if (id === 'start_speech') {
        return { type: 'start_speech' };
    }
    if (id === 'start_vote') {
        return { type: 'start_vote' };
    }
    if (id === 'reveal_idiot') {
        return { type: 'reveal_idiot' };
    }
    return { type: 'accept_exile' };
}

function mountGameApp(): void {
    const canvas = director.getScene()?.getChildByName('Canvas');
    if (!canvas || canvas.getComponent(GameApp)) {
        return;
    }
    canvas.addComponent(GameApp);
}

const mountFlag = '__lrsMountGameApp';
const globalFlags = globalThis as typeof globalThis & { [mountFlag]?: boolean };
if (!globalFlags[mountFlag]) {
    globalFlags[mountFlag] = true;
    director.on(Director.EVENT_AFTER_SCENE_LAUNCH, mountGameApp);
}
mountGameApp();
