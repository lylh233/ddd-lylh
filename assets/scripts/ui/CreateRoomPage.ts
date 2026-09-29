import { EditBox, Node } from 'cc';
import { Theme } from './theme';
import { createButton, createEditBox, createLabel, createNode, place } from './UiFactory';

export class CreateRoomPage {
    readonly node: Node;
    readonly roomBox: EditBox;

    constructor(parent: Node, width: number, height: number, onCreate: () => void, onJoin: (roomId: string) => void, onBack: () => void) {
        this.node = createNode(parent, 'create-room', width, height);
        const title = createLabel(this.node, '创建房间', 48, Theme.title, width, 72);
        place(title.node, height, 180);
        const create = createButton(this.node, '创建房间', Math.min(560, width - 80), 88, Theme.primary, onCreate);
        place(create, height, 320);
        const roomTitle = createLabel(this.node, '房间号', 32, Theme.title, width, 48);
        place(roomTitle.node, height, 470);
        this.roomBox = createEditBox(this.node, Math.min(560, width - 80), 88, '', '输入房间号', 16);
        place(this.roomBox.node, height, 540);
        const join = createButton(this.node, '加入房间', Math.min(560, width - 80), 88, Theme.confirm, () => onJoin(this.roomBox.string.trim()));
        place(join, height, 670);
        const back = createButton(this.node, '返回', Math.min(560, width - 80), 88, Theme.secondary, onBack);
        place(back, height, 800);
    }
}
