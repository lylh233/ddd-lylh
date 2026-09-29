import { EditBox, Node } from 'cc';
import { Theme } from './theme';
import { createButton, createEditBox, createLabel, createNode, place } from './UiFactory';

export class HomePage {
    readonly node: Node;
    readonly nameBox: EditBox;

    constructor(parent: Node, width: number, height: number, name: string, onName: (value: string) => void, onEmpty: () => void, onLocal: () => void, onOnline: () => void) {
        this.node = createNode(parent, 'home', width, height);
        const title = createLabel(this.node, '狼人杀', 48, Theme.title, width, 72);
        place(title.node, height, 220);
        const nameTitle = createLabel(this.node, '玩家名字', 32, Theme.title, width, 48);
        place(nameTitle.node, height, 360);
        this.nameBox = createEditBox(this.node, Math.min(560, width - 80), 88, name, '请输入玩家名称', 8, true);
        place(this.nameBox.node, height, 430);
        this.nameBox.node.on('editing-did-ended', () => onName(this.nameBox.string.trim()));
        const enter = (next: () => void) => {
            const value = this.nameBox.string.trim();
            if (!value) {
                onEmpty();
                return;
            }
            onName(value);
            next();
        };
        const local = createButton(this.node, '人机对局', Math.min(560, width - 80), 88, Theme.primary, () => enter(onLocal));
        place(local, height, 620);
        const online = createButton(this.node, '好友联机', Math.min(560, width - 80), 88, Theme.confirm, () => enter(onOnline));
        place(online, height, 740);
    }
}
