import { _decorator, Canvas, Component, ResolutionPolicy, screen, view } from 'cc';
import { SafeAreaAdapter, SafeInsets } from './SafeAreaAdapter';

const { ccclass } = _decorator;

@ccclass('ScreenAdapter')
export class ScreenAdapter extends Component {
    static instance: ScreenAdapter | null = null;
    static readonly DESIGN_WIDTH = 750;
    static readonly DESIGN_HEIGHT = 1334;

    private canvas: Canvas | null = null;
    private applying = false;
    private listeners: (() => void)[] = [];

    onLoad(): void {
        ScreenAdapter.instance = this;
        this.canvas = this.getComponent(Canvas);
        if (this.canvas) {
            this.canvas.alignCanvasWithScreen = true;
        }
        this.apply();
        view.on('canvas-resize', this.apply, this);
        screen.on('window-resize', this.apply, this);
    }

    onDestroy(): void {
        view.off('canvas-resize', this.apply, this);
        screen.off('window-resize', this.apply, this);
        if (ScreenAdapter.instance === this) {
            ScreenAdapter.instance = null;
        }
    }

    onChange(listener: () => void): void {
        this.listeners.push(listener);
    }

    apply(): void {
        if (this.applying) {
            return;
        }
        const size = screen.windowSize;
        if (size.width <= 0 || size.height <= 0) {
            return;
        }
        const screenRatio = size.width / size.height;
        const designRatio = ScreenAdapter.DESIGN_WIDTH / ScreenAdapter.DESIGN_HEIGHT;
        const policy = screenRatio < designRatio ? ResolutionPolicy.FIXED_HEIGHT : ResolutionPolicy.FIXED_WIDTH;
        this.applying = true;
        view.setDesignResolutionSize(ScreenAdapter.DESIGN_WIDTH, ScreenAdapter.DESIGN_HEIGHT, policy);
        this.applying = false;
        this.listeners.forEach((listener) => listener());
    }

    getSafeArea(): SafeInsets {
        return SafeAreaAdapter.getSafeAreaInsets();
    }

    getTopSafePadding(): number {
        return SafeAreaAdapter.getTopPadding();
    }
}
