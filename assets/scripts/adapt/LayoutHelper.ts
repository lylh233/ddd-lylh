import { Node, Widget } from 'cc';

export type HorizontalAlign = 'left' | 'center' | 'right';
export type VerticalAlign = 'top' | 'center' | 'bottom';

export interface WidgetOptions {
    top?: number;
    bottom?: number;
    left?: number;
    right?: number;
    alignMode?: number;
    horizontal?: HorizontalAlign;
    vertical?: VerticalAlign;
}

export class LayoutHelper {
    static attachWidget(node: Node, options: WidgetOptions): Widget {
        const widget = node.getComponent(Widget) || node.addComponent(Widget);
        widget.alignMode = options.alignMode ?? Widget.AlignMode.ON_WINDOW_RESIZE;
        widget.isAlignTop = false;
        widget.isAlignBottom = false;
        widget.isAlignLeft = false;
        widget.isAlignRight = false;
        widget.isAlignHorizontalCenter = false;
        widget.isAlignVerticalCenter = false;
        if (options.horizontal === 'center') {
            widget.isAlignHorizontalCenter = true;
            widget.horizontalCenter = 0;
        }
        if (options.vertical === 'center') {
            widget.isAlignVerticalCenter = true;
            widget.verticalCenter = 0;
        }
        if (options.top != null) {
            widget.isAlignTop = true;
            widget.top = options.top;
        }
        if (options.bottom != null) {
            widget.isAlignBottom = true;
            widget.bottom = options.bottom;
        }
        if (options.left != null) {
            widget.isAlignLeft = true;
            widget.left = options.left;
        }
        if (options.right != null) {
            widget.isAlignRight = true;
            widget.right = options.right;
        }
        widget.updateAlignment();
        return widget;
    }

    static pinToTop(node: Node, padding: number): Widget {
        return LayoutHelper.attachWidget(node, { top: padding, left: 0, right: 0 });
    }

    static pinToBottom(node: Node, padding: number): Widget {
        return LayoutHelper.attachWidget(node, { bottom: padding, left: 0, right: 0 });
    }

    static pinToLeft(node: Node, padding: number): Widget {
        return LayoutHelper.attachWidget(node, { left: padding, top: 0, bottom: 0 });
    }

    static pinToRight(node: Node, padding: number): Widget {
        return LayoutHelper.attachWidget(node, { right: padding, top: 0, bottom: 0 });
    }

    static centerInParent(node: Node): Widget {
        return LayoutHelper.attachWidget(node, { horizontal: 'center', vertical: 'center' });
    }
}
