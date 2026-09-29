import {
    BlockInputEvents,
    Color,
    EditBox,
    EventTouch,
    Graphics,
    Label,
    Layers,
    Layout,
    Mask,
    Node,
    ScrollView,
    UITransform,
} from 'cc';
import { Theme } from './theme';

export function createNode(parent: Node | null, name: string, width: number, height: number): Node {
    const node = new Node(name);
    node.layer = Layers.Enum.UI_2D;
    const transform = node.addComponent(UITransform);
    transform.setAnchorPoint(0.5, 0.5);
    transform.setContentSize(width, height);
    if (parent) {
        parent.addChild(node);
    }
    return node;
}

export function paint(node: Node, fill: Color, radius = 16, stroke?: Color): void {
    const transform = node.getComponent(UITransform)!;
    const graphics = node.getComponent(Graphics) || node.addComponent(Graphics);
    const width = transform.width;
    const height = transform.height;
    graphics.clear();
    graphics.fillColor = fill;
    graphics.roundRect(-width / 2, -height / 2, width, height, radius);
    graphics.fill();
    if (stroke) {
        graphics.strokeColor = stroke;
        graphics.lineWidth = 2;
        graphics.roundRect(-width / 2, -height / 2, width, height, radius);
        graphics.stroke();
    }
}

export function createLabel(parent: Node, text: string, size: number, color: Color, width?: number, height?: number): Label {
    const node = new Node('label');
    node.layer = Layers.Enum.UI_2D;
    parent.addChild(node);
    const parentTransform = parent.getComponent(UITransform);
    const transform = node.addComponent(UITransform);
    transform.setContentSize(width ?? parentTransform?.width ?? 100, height ?? parentTransform?.height ?? size + 8);
    const label = node.addComponent(Label);
    label.string = text;
    label.fontSize = size;
    label.lineHeight = size + 8;
    label.color = color;
    label.useSystemFont = true;
    label.horizontalAlign = Label.HorizontalAlign.CENTER;
    label.verticalAlign = Label.VerticalAlign.CENTER;
    label.overflow = Label.Overflow.SHRINK;
    label.enableWrapText = true;
    return label;
}

export function place(node: Node, parentHeight: number, top: number): void {
    const height = node.getComponent(UITransform)!.height;
    node.setPosition(0, parentHeight / 2 - top - height / 2, 0);
}

export function createButton(parent: Node, text: string, width: number, height: number, fill: Color, onClick: () => void, fontSize = 30): Node {
    const node = createNode(parent, 'button', width, height);
    paint(node, fill, 16, Theme.stroke);
    createLabel(node, text, fontSize, Theme.text, width - 12, height);
    node.addComponent(BlockInputEvents);
    node.on(Node.EventType.TOUCH_END, (event: EventTouch) => {
        event.propagationStopped = true;
        onClick();
    });
    return node;
}

export function createEditBox(parent: Node, width: number, height: number, value: string, placeholder: string, maxLength: number, centered = false): EditBox {
    const node = createNode(parent, 'edit', width, height);
    paint(node, Theme.secondary, 16, Theme.stroke);
    const align = centered ? Label.HorizontalAlign.CENTER : Label.HorizontalAlign.LEFT;
    const textNode = new Node('text');
    textNode.layer = Layers.Enum.UI_2D;
    node.addChild(textNode);
    const textTransform = textNode.addComponent(UITransform);
    textTransform.setAnchorPoint(centered ? 0.5 : 0, 0.5);
    textTransform.setContentSize(centered ? width : width - 32, height);
    textNode.setPosition(centered ? 0 : -width / 2 + 16, 0, 0);
    const text = textNode.addComponent(Label);
    text.fontSize = 26;
    text.lineHeight = height;
    text.color = Theme.text;
    text.useSystemFont = true;
    text.horizontalAlign = align;
    text.verticalAlign = Label.VerticalAlign.CENTER;
    text.overflow = Label.Overflow.CLAMP;

    const placeholderNode = new Node('placeholder');
    placeholderNode.layer = Layers.Enum.UI_2D;
    node.addChild(placeholderNode);
    const placeholderTransform = placeholderNode.addComponent(UITransform);
    placeholderTransform.setAnchorPoint(centered ? 0.5 : 0, 0.5);
    placeholderTransform.setContentSize(centered ? width : width - 32, height);
    placeholderNode.setPosition(centered ? 0 : -width / 2 + 16, 0, 0);
    const placeholderLabel = placeholderNode.addComponent(Label);
    placeholderLabel.fontSize = 26;
    placeholderLabel.lineHeight = height;
    placeholderLabel.color = Theme.sub;
    placeholderLabel.useSystemFont = true;
    placeholderLabel.horizontalAlign = align;
    placeholderLabel.verticalAlign = Label.VerticalAlign.CENTER;

    const edit = node.addComponent(EditBox);
    edit.textLabel = text;
    edit.placeholderLabel = placeholderLabel;
    node.getChildByName('TEXT_LABEL')?.destroy();
    node.getChildByName('PLACEHOLDER_LABEL')?.destroy();
    edit.placeholder = placeholder;
    edit.string = value;
    edit.maxLength = maxLength;
    edit.inputMode = EditBox.InputMode.SINGLE_LINE;
    text.horizontalAlign = align;
    text.verticalAlign = Label.VerticalAlign.CENTER;
    placeholderLabel.horizontalAlign = align;
    placeholderLabel.verticalAlign = Label.VerticalAlign.CENTER;
    return edit;
}

export function createScroll(parent: Node, width: number, height: number, columns = 1): Node {
    const root = createNode(parent, 'scroll', width, height);
    root.addComponent(Mask);
    const scroll = root.addComponent(ScrollView);
    scroll.vertical = true;
    scroll.horizontal = false;
    scroll.inertia = true;
    const content = createNode(root, 'content', width, height);
    const contentTransform = content.getComponent(UITransform)!;
    contentTransform.setAnchorPoint(0.5, 1);
    content.setPosition(0, height / 2, 0);
    const layout = content.addComponent(Layout);
    layout.resizeMode = Layout.ResizeMode.CONTAINER;
    layout.paddingTop = 8;
    layout.paddingBottom = 8;
    layout.spacingY = 12;
    if (columns > 1) {
        layout.type = Layout.Type.GRID;
        layout.startAxis = Layout.AxisDirection.HORIZONTAL;
        layout.constraint = Layout.Constraint.FIXED_COL;
        layout.constraintNum = columns;
        layout.spacingX = 12;
        layout.paddingLeft = 8;
        layout.paddingRight = 8;
    } else {
        layout.type = Layout.Type.VERTICAL;
    }
    scroll.content = content;
    return content;
}

export class ModalHost {
    readonly layer: Node;

    constructor(parent: Node, width: number, height: number) {
        this.layer = createNode(parent, 'modal', width, height);
        this.layer.active = false;
    }

    resize(width: number, height: number): void {
        this.layer.getComponent(UITransform)!.setContentSize(width, height);
        this.bringToFront();
    }

    bringToFront(): void {
        this.layer.setSiblingIndex(this.layer.parent ? this.layer.parent.children.length - 1 : 0);
    }

    clear(): void {
        this.layer.removeAllChildren();
        this.layer.active = false;
    }

    alert(text: string, onOk?: () => void): void {
        this.confirm(text, () => onOk?.(), undefined, '知道了', '');
    }

    confirm(text: string, onYes: () => void, onNo?: () => void, yesText = '确定', noText = '取消'): void {
        this.clear();
        this.layer.active = true;
        this.bringToFront();
        const size = this.layer.getComponent(UITransform)!;
        const dim = createNode(this.layer, 'dim', size.width, size.height);
        paint(dim, Theme.dim, 0);
        dim.addComponent(BlockInputEvents);
        const panel = createNode(this.layer, 'panel', Math.min(620, size.width - 48), 320);
        paint(panel, Theme.panel, 16, Theme.stroke);
        createLabel(panel, text, 26, Theme.text, panel.getComponent(UITransform)!.width - 40, 150);
        const label = panel.getChildByName('label')!;
        label.setPosition(0, 50, 0);
        const yes = createButton(panel, yesText, noText ? 220 : 280, 88, Theme.confirm, () => {
            this.clear();
            onYes();
        });
        yes.setPosition(noText ? -130 : 0, -90, 0);
        if (noText) {
            const no = createButton(panel, noText, 220, 88, Theme.secondary, () => {
                this.clear();
                onNo?.();
            });
            no.setPosition(130, -90, 0);
        }
    }

    pick(title: string, options: { target: number | null; label: string }[], onPick: (target: number | null) => void): void {
        this.clear();
        this.layer.active = true;
        this.bringToFront();
        const size = this.layer.getComponent(UITransform)!;
        const dim = createNode(this.layer, 'dim', size.width, size.height);
        paint(dim, Theme.dim, 0);
        dim.addComponent(BlockInputEvents);
        dim.on(Node.EventType.TOUCH_END, () => this.clear());
        const panelWidth = Math.min(640, size.width - 40);
        const panelHeight = Math.min(860, size.height - 80);
        const panel = createNode(this.layer, 'panel', panelWidth, panelHeight);
        paint(panel, Theme.panel, 16, Theme.stroke);
        const titleLabel = createLabel(panel, title, 32, Theme.title, panelWidth - 24, 56);
        titleLabel.node.setPosition(0, panelHeight / 2 - 40, 0);
        const content = createScroll(panel, panelWidth - 24, panelHeight - 150, 2);
        content.parent!.setPosition(0, -20, 0);
        const optionWidth = Math.floor((panelWidth - 24 - 16 - 12) / 2);
        options.forEach((option) => {
            const button = createButton(content, option.label, optionWidth, 88, Theme.secondary, () => {
                this.clear();
                onPick(option.target);
            }, 22);
            button.getComponent(UITransform)!.setContentSize(optionWidth, 88);
        });
        content.getComponent(Layout)?.updateLayout(true);
        const close = createButton(panel, '关闭', 180, 64, Theme.primary, () => this.clear(), 24);
        close.setPosition(0, -panelHeight / 2 + 42, 0);
    }
}
