import { Color, Label, Node } from 'cc';
import { Theme } from './theme';
import { createLabel, createNode, paint } from './UiFactory';

export function buildPlayerCard(parent: Node, width: number, height: number, title: string, detail: string, dead: boolean, onClick?: () => void, badge = ''): Node {
    const node = createNode(parent, 'seat', width, height);
    paint(node, dead ? new Color(0x24, 0x1c, 0x16, 255) : Theme.panel, 16, Theme.stroke);
    const badgeWidth = badge ? 92 : 0;
    if (badge) {
        const mark = createLabel(node, badge, 20, Theme.title, badgeWidth, 32);
        mark.horizontalAlign = Label.HorizontalAlign.LEFT;
        mark.node.setPosition(-width / 2 + 10 + badgeWidth / 2, 0, 0);
    }
    const textWidth = width - 24 - badgeWidth;
    const titleLabel = createLabel(node, title, 26, dead ? Theme.dead : Theme.text, textWidth, Math.max(32, height * 0.45));
    titleLabel.node.setPosition(badgeWidth / 2, detail ? 14 : 0, 0);
    if (detail) {
        const detailLabel = createLabel(node, detail, 22, dead ? Theme.dead : Theme.sub, textWidth, 30);
        detailLabel.node.setPosition(badgeWidth / 2, -18, 0);
    }
    if (onClick) {
        node.on(Node.EventType.TOUCH_END, (event) => {
            event.propagationStopped = true;
            onClick();
        });
    }
    return node;
}
