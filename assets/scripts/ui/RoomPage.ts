import { BlockInputEvents, EventTouch, Layout, Node, UITransform, Vec3 } from 'cc';
import { ROLE_LIST, RoleId } from '../game/types';
import { MAX_PLAYER_COUNT, MIN_PLAYER_COUNT } from '../game/presets';
import { Theme } from './theme';
import { createButton, createLabel, createNode, createScroll, paint } from './UiFactory';

export interface RoomSeatInfo {
    seat: number;
    name: string;
    filled: boolean;
}

export class RoomPage {
    readonly node: Node;

    constructor(parent: Node, width: number, height: number, state: {
        roomId: string;
        playerCount: number;
        roles: RoleId[];
        seats: RoomSeatInfo[];
        canEdit: boolean;
        online: boolean;
    }, handlers: {
        onCount: (count: number) => void;
        onRoles: (roles: RoleId[]) => void;
        onStart: () => void;
        onBack: () => void;
    }) {
        this.node = createNode(parent, 'room', width, height);
        const title = createLabel(this.node, '房间', 48, Theme.title, width, 64);
        title.node.setPosition(0, height / 2 - 56, 0);
        let cursor = 100;
        if (state.roomId) {
            const room = createLabel(this.node, `房间号 ${state.roomId}`, 26, Theme.text, width, 36);
            room.node.setPosition(0, height / 2 - cursor, 0);
            cursor += 46;
        }
        const counts = tally(state.roles);
        const selected = state.roles.length;
        cursor += buildCountSlider(this.node, width, height, cursor, state.playerCount, state.canEdit, handlers.onCount);
        const filled = state.seats.filter((seat) => seat.filled).length;
        const seatText = state.online
            ? `入座 ${filled}/${state.playerCount}  ${state.seats.filter((seat) => seat.filled).map((seat) => seat.name).join('、')}`
            : `你与 ${Math.max(0, state.playerCount - 1)} 名电脑玩家`;
        const seats = createLabel(this.node, seatText, 22, Theme.sub, width - 40, 56);
        seats.node.setPosition(0, height / 2 - cursor, 0);
        cursor += 50;
        const identity = createLabel(this.node, `身份列表（已选${selected}/${state.playerCount}）`, 32, Theme.title, width - 40, 44);
        identity.node.setPosition(0, height / 2 - cursor, 0);
        cursor += 36;
        const scrollHeight = Math.max(220, height - cursor - 230);
        const content = createScroll(this.node, width - 24, scrollHeight, 2);
        content.parent!.setPosition(0, height / 2 - cursor - scrollHeight / 2, 0);
        const cardWidth = Math.floor((width - 24 - 16 - 12) / 2);
        ROLE_LIST.forEach((role) => {
            const row = createNode(content, role.id, cardWidth, 176);
            paint(row, Theme.panel, 16, Theme.stroke);
            const name = createLabel(row, role.name, 26, Theme.title, cardWidth - 20, 34);
            name.node.setPosition(0, 58, 0);
            const desc = createLabel(row, role.desc, 20, Theme.sub, cardWidth - 20, 40);
            desc.node.setPosition(0, 22, 0);
            const countLabel = createLabel(row, `${counts[role.id]}`, 28, Theme.text, 48, 40);
            countLabel.node.setPosition(0, -48, 0);
            if (state.canEdit) {
                const minus = createButton(row, '-', 52, 52, Theme.secondary, () => {
                    if (counts[role.id] > 0) {
                        handlers.onRoles(expand(counts, role.id, -1));
                    }
                }, 28);
                minus.setPosition(-62, -48, 0);
                const plus = createButton(row, '+', 52, 52, Theme.confirm, () => {
                    if (selected < state.playerCount) {
                        handlers.onRoles(expand(counts, role.id, 1));
                    }
                }, 28);
                plus.setPosition(62, -48, 0);
            }
        });
        content.getComponent(Layout)?.updateLayout(true);
        const startFill = selected === state.playerCount ? Theme.primary : Theme.secondary;
        const start = createButton(this.node, '开始对局', Math.min(560, width - 80), 88, startFill, handlers.onStart);
        start.setPosition(0, -height / 2 + 150, 0);
        const back = createButton(this.node, '返回', Math.min(560, width - 80), 88, Theme.secondary, handlers.onBack);
        back.setPosition(0, -height / 2 + 52, 0);
    }
}

function buildCountSlider(parent: Node, width: number, height: number, cursor: number, playerCount: number, canEdit: boolean, onCount: (count: number) => void): number {
    const sliderWidth = width - 48;
    const rowHeight = 108;
    const row = createNode(parent, 'counts', sliderWidth, rowHeight);
    row.setPosition(0, height / 2 - cursor - rowHeight / 2, 0);
    const label = createLabel(row, `${playerCount}人局`, 28, Theme.title, sliderWidth, 36);
    label.node.setPosition(0, 32, 0);
    const minLabel = createLabel(row, `${MIN_PLAYER_COUNT}`, 20, Theme.sub, 40, 28);
    minLabel.node.setPosition(-sliderWidth / 2 + 20, 32, 0);
    const maxLabel = createLabel(row, `${MAX_PLAYER_COUNT}`, 20, Theme.sub, 40, 28);
    maxLabel.node.setPosition(sliderWidth / 2 - 20, 32, 0);
    const trackWidth = sliderWidth;
    const track = createNode(row, 'track', trackWidth, 12);
    track.setPosition(0, -22, 0);
    paint(track, Theme.secondary, 6);
    const handleSize = 44;
    const travel = trackWidth - handleSize;
    const fill = createNode(row, 'fill', handleSize / 2, 12);
    fill.setPosition(-trackWidth / 2 + handleSize / 4, -22, 0);
    const handle = createNode(row, 'handle', handleSize, handleSize);
    paint(handle, canEdit ? Theme.primary : Theme.dead, 22, Theme.stroke);
    const xOf = (count: number) => -travel / 2 + ((count - MIN_PLAYER_COUNT) / (MAX_PLAYER_COUNT - MIN_PLAYER_COUNT)) * travel;
    const place = (count: number) => {
        const x = xOf(count);
        handle.setPosition(x, -22, 0);
        const fillWidth = Math.max(12, x + trackWidth / 2);
        fill.getComponent(UITransform)!.setContentSize(fillWidth, 12);
        fill.setPosition(-trackWidth / 2 + fillWidth / 2, -22, 0);
        paint(fill, Theme.primary, 6);
        label.string = `${count}人局`;
    };
    place(playerCount);
    if (canEdit) {
        row.addComponent(BlockInputEvents);
        let current = playerCount;
        const read = (event: EventTouch) => {
            const point = event.getUILocation();
            const local = row.getComponent(UITransform)!.convertToNodeSpaceAR(new Vec3(point.x, point.y, 0));
            const ratio = Math.min(1, Math.max(0, (local.x + travel / 2) / travel));
            current = MIN_PLAYER_COUNT + Math.round(ratio * (MAX_PLAYER_COUNT - MIN_PLAYER_COUNT));
            place(current);
        };
        const commit = (event: EventTouch) => {
            event.propagationStopped = true;
            read(event);
            if (current !== playerCount) {
                onCount(current);
            }
        };
        row.on(Node.EventType.TOUCH_START, (event: EventTouch) => {
            event.propagationStopped = true;
            read(event);
        });
        row.on(Node.EventType.TOUCH_MOVE, (event: EventTouch) => {
            event.propagationStopped = true;
            read(event);
        });
        row.on(Node.EventType.TOUCH_END, commit);
        row.on(Node.EventType.TOUCH_CANCEL, commit);
    }
    return rowHeight + 16;
}

function tally(roles: RoleId[]): Record<RoleId, number> {
    const counts = {} as Record<RoleId, number>;
    ROLE_LIST.forEach((role) => {
        counts[role.id] = 0;
    });
    roles.forEach((role) => {
        counts[role] += 1;
    });
    return counts;
}

function expand(counts: Record<RoleId, number>, id: RoleId, delta: number): RoleId[] {
    const next = { ...counts, [id]: Math.max(0, counts[id] + delta) };
    const roles: RoleId[] = [];
    ROLE_LIST.forEach((role) => {
        for (let index = 0; index < next[role.id]; index += 1) {
            roles.push(role.id);
        }
    });
    return roles;
}
