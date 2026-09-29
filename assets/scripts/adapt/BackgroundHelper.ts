import { Color, Graphics, Layers, Node, UITransform } from 'cc';
import { LayoutHelper } from './LayoutHelper';

const BACKGROUND = new Color(0x1c, 0x14, 0x10, 255);

export class BackgroundHelper {
    static setBackground(node: Node): void {
        node.layer = Layers.Enum.UI_2D;
        const transform = node.getComponent(UITransform) || node.addComponent(UITransform);
        transform.setAnchorPoint(0.5, 0.5);
        transform.setContentSize(750, 1334);
        const redraw = () => {
            const graphics = node.getComponent(Graphics) || node.addComponent(Graphics);
            const width = transform.width;
            const height = transform.height;
            graphics.clear();
            graphics.fillColor = BACKGROUND;
            graphics.rect(-width / 2, -height / 2, width, height);
            graphics.fill();
        };
        LayoutHelper.attachWidget(node, { top: 0, bottom: 0, left: 0, right: 0 });
        node.on(Node.EventType.SIZE_CHANGED, redraw);
        redraw();
    }
}
