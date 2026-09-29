import { Label, Layout, Node, UITransform } from 'cc';
import { GameView, Phase, roleInfo } from '../game/types';
import { Theme } from './theme';
import { buildPlayerCard } from './SeatCard';
import { createButton, createLabel, createNode, createScroll, paint } from './UiFactory';

export class GamePage {
    readonly node: Node;

    constructor(parent: Node, width: number, height: number, view: GameView, safeTop: number, safeBottom: number, logOpen: boolean, logDay: number, handlers: {
        onAction: (id: string) => void;
        onSelf: () => void;
        onToggleLog: () => void;
        onLogDay: (day: number) => void;
    }) {
        this.node = createNode(parent, 'game', width, height);
        const topInset = Math.max(0, safeTop);
        const bottomInset = Math.max(0, safeBottom);
        const topHeight = 120;
        const bottomHeight = 314;
        const middleHeight = Math.max(180, height - topInset - bottomInset - topHeight - bottomHeight);

        const top = createNode(this.node, 'top', width - 24, topHeight);
        paint(top, Theme.panel, 16, Theme.stroke);
        top.setPosition(0, height / 2 - topInset - topHeight / 2, 0);
        createLabel(top, `第${view.day}天  ${sceneName(view.phase)}`, 32, Theme.title, width - 48, 64);

        const middle = createNode(this.node, 'middle', width - 16, middleHeight);
        middle.setPosition(0, height / 2 - topInset - topHeight - middleHeight / 2, 0);
        this.buildSeats(middle, width - 32, middleHeight, view, handlers.onSelf);

        const bottom = createNode(this.node, 'bottom', width - 16, bottomHeight);
        paint(bottom, Theme.panel, 16, Theme.stroke);
        bottom.setPosition(0, -height / 2 + bottomInset + bottomHeight / 2, 0);
        this.buildActions(bottom, width - 32, view, handlers.onAction, handlers.onToggleLog);

        if (logOpen) {
            showLogPanel(this.node, width, height, view, logDay, handlers.onToggleLog, handlers.onLogDay);
        }
    }

    private buildSeats(middle: Node, width: number, height: number, view: GameView, onSelf: () => void): void {
        let listHeight = height;
        if (view.voteText) {
            const banner = createNode(middle, 'votes', width, 72);
            paint(banner, Theme.paper, 12);
            banner.setPosition(0, height / 2 - 40, 0);
            const summary = createLabel(banner, view.voteText, 22, Theme.ink, width - 16, 64);
            summary.overflow = Label.Overflow.SHRINK;
            listHeight = Math.max(160, height - 84);
        }
        const content = createScroll(middle, width, listHeight, 2);
        if (view.voteText) {
            content.parent!.setPosition(0, -42, 0);
        }
        const cardWidth = Math.floor((width - 16 - 12) / 2);
        view.seats.forEach((seat) => this.card(content, cardWidth, 112, view, seat.seat, onSelf));
        content.getComponent(Layout)?.updateLayout(true);
    }

    private card(parent: Node, width: number, height: number, view: GameView, seatNumber: number, onSelf: () => void): Node {
        const seat = view.seats[seatNumber - 1];
        const deadText = !seat.alive ? (seat.deadReason === 'exiled' ? '被放逐' : '被淘汰') : '';
        const marks = [
            seat.role ? roleInfo(seat.role).name : '',
            seat.isSheriff ? '警长' : '',
            seat.campaigning ? '上警' : '',
            seat.revealedIdiot ? '隐神' : '',
            deadText,
        ];
        const knife = seat.knifeBy.length ? seat.knifeBy.map((item) => `${item}号刀`).join(' ') : '';
        const ballot = view.ballots.find((item) => item.seat === seat.seat);
        const voteMark = ballot ? (ballot.target == null ? '弃权' : `投给${ballot.target}号`) : '';
        const detail = [marks.filter(Boolean).join('  '), knife, voteMark].filter(Boolean).join('  ');
        return buildPlayerCard(parent, width, height, `${seat.seat}号-${seat.name}`, detail, !seat.alive, () => {
            if (seat.seat === view.selfSeat && view.phase !== 'settlement') {
                onSelf();
            }
        }, voteBadge(view, seat.seat));
    }

    private buildActions(bottom: Node, width: number, view: GameView, onAction: (id: string) => void, onLog: () => void): void {
        const actions = view.actions;
        const columns = Math.min(4, Math.max(1, actions.length));
        const rows = Math.max(1, Math.ceil(actions.length / columns));
        const buttonHeight = actions.length > 4 ? 64 : 88;
        const buttonWidth = actions.length ? Math.floor((width - 16 - (columns - 1) * 8) / columns) : 120;
        const block = rows * buttonHeight + Math.max(0, rows - 1) * 8;
        actions.forEach((action, index) => {
            const column = index % columns;
            const row = Math.floor(index / columns);
            const button = createButton(bottom, action.label, buttonWidth, buttonHeight, action.urgent ? Theme.danger : Theme.primary, () => onAction(action.id), actions.length > 4 ? 22 : 26);
            const x = -width / 2 + 8 + buttonWidth / 2 + column * (buttonWidth + 8);
            const y = 145 - buttonHeight / 2 - row * (buttonHeight + 8);
            button.setPosition(x, y, 0);
        });
        const prompt = createLabel(bottom, view.prompt, 22, Theme.text, width - 16, 56);
        prompt.node.setPosition(0, 145 - block - 36, 0);
        const log = createButton(bottom, '日志', Math.min(220, width - 40), 64, Theme.secondary, onLog, 24);
        log.setPosition(0, -118, 0);
    }

}

export function showLogPanel(parent: Node, width: number, height: number, view: GameView, logDay: number, onClose: () => void, onDay: (day: number) => void): void {
    const mask = createNode(parent, 'log', width, height);
    paint(mask, Theme.dim, 0);
    const panel = createNode(mask, 'panel', width - 24, height - 80);
    paint(panel, Theme.bg, 16, Theme.stroke);
    const days = uniqueDays(view);
    const selected = days.indexOf(logDay) >= 0 ? logDay : days[days.length - 1];
    const dayColumn = createNode(panel, 'days', 150, panel.getComponent(UITransform)!.height - 110);
    dayColumn.setPosition(-panel.getComponent(UITransform)!.width / 2 + 90, 10, 0);
    days.forEach((day, index) => {
        const button = createButton(dayColumn, `第${day}日`, 130, 56, day === selected ? Theme.primary : Theme.secondary, () => onDay(day), 22);
        button.setPosition(0, dayColumn.getComponent(UITransform)!.height / 2 - 40 - index * 66, 0);
    });
    const logs = view.logs.filter((entry) => entry.day === selected);
    const content = createScroll(panel, panel.getComponent(UITransform)!.width - 190, panel.getComponent(UITransform)!.height - 110);
    content.parent!.setPosition(80, 10, 0);
    if (!logs.length) {
        createLabel(content, '这一天还没有日志', 22, Theme.sub, 360, 40);
    }
    logs.forEach((entry) => {
        const row = createNode(content, 'log-row', panel.getComponent(UITransform)!.width - 220, 72);
        paint(row, Theme.paper, 12);
        const label = createLabel(row, entry.text, 22, Theme.ink, row.getComponent(UITransform)!.width - 16, 64);
        label.horizontalAlign = Label.HorizontalAlign.LEFT;
    });
    content.getComponent(Layout)?.updateLayout(true);
    const close = createButton(panel, '关闭日志', 220, 64, Theme.primary, onClose, 24);
    close.setPosition(0, -panel.getComponent(UITransform)!.height / 2 + 42, 0);
}

function voteBadge(view: GameView, seat: number): string {
    if (view.phase !== 'day_sheriff_vote' && view.phase !== 'day_exile_vote') {
        return '';
    }
    let halves = 0;
    view.ballots.forEach((ballot) => {
        if (ballot.target !== seat) {
            return;
        }
        const voter = view.seats[ballot.seat - 1];
        halves += view.phase === 'day_exile_vote' && voter?.isSheriff ? 3 : 2;
    });
    if (halves <= 0) {
        return '';
    }
    const votes = halves / 2;
    const text = votes % 1 === 0 ? String(votes) : votes.toFixed(1);
    return `（${text}票）`;
}

function sceneName(phase: Phase): string {
    if (phase.indexOf('night') === 0) {
        return '黑夜';
    }
    if (phase === 'settlement') {
        return '结算';
    }
    return '白天';
}

function uniqueDays(view: GameView): number[] {
    const days: number[] = [];
    view.logs.forEach((entry) => {
        if (days.indexOf(entry.day) < 0) {
            days.push(entry.day);
        }
    });
    if (!days.length) {
        days.push(view.day);
    }
    return days;
}
