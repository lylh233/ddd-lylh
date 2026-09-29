import { Layout, Node } from 'cc';
import { GameView, roleInfo } from '../game/types';
import { Theme } from './theme';
import { showLogPanel } from './GamePage';
import { buildPlayerCard } from './SeatCard';
import { createButton, createLabel, createNode, createScroll } from './UiFactory';

export class SettlementPage {
    readonly node: Node;

    constructor(parent: Node, width: number, height: number, view: GameView, logOpen: boolean, logDay: number, onHome: () => void, onLog: () => void, onLogDay: (day: number) => void) {
        this.node = createNode(parent, 'settlement', width, height);
        const title = createLabel(this.node, view.prompt || '对局结束', 48, Theme.title, width - 40, 72);
        title.node.setPosition(0, height / 2 - 80, 0);
        const content = createScroll(this.node, width - 32, height - 360, 2);
        content.parent!.setPosition(0, 40, 0);
        const cardWidth = Math.floor((width - 32 - 16 - 12) / 2);
        view.seats.forEach((seat) => {
            const roleName = seat.role ? roleInfo(seat.role).name : '';
            const state = !seat.alive ? (seat.deadReason === 'exiled' ? '被放逐' : '被淘汰') : '存活';
            const marks = [roleName, seat.isSheriff ? '警长' : '', seat.revealedIdiot ? '隐神' : '', state].filter(Boolean).join('  ');
            buildPlayerCard(content, cardWidth, 112, `${seat.seat}号-${seat.name}`, marks, !seat.alive);
        });
        content.getComponent(Layout)?.updateLayout(true);
        const log = createButton(this.node, '日志', Math.min(560, width - 80), 88, Theme.secondary, onLog);
        log.setPosition(0, -height / 2 + 160, 0);
        const home = createButton(this.node, '返回首页', Math.min(560, width - 80), 88, Theme.confirm, onHome);
        home.setPosition(0, -height / 2 + 60, 0);
        if (logOpen) {
            showLogPanel(this.node, width, height, view, logDay, onLog, onLogDay);
        }
    }
}
