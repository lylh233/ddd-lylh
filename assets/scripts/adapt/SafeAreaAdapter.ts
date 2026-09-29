import { Node, view, Widget } from 'cc';
import { LayoutHelper, WidgetOptions } from './LayoutHelper';

export interface SafeInsets {
    top: number;
    bottom: number;
    left: number;
    right: number;
}

function hasWx(): boolean {
    return typeof wx !== 'undefined' && typeof wx.getSystemInfoSync === 'function';
}

function designScale(windowWidth: number): number {
    if (!windowWidth) {
        return 1;
    }
    return view.getVisibleSize().width / windowWidth;
}

export class SafeAreaAdapter {
    static getSafeAreaInsets(): SafeInsets {
        if (!hasWx()) {
            return { top: 0, bottom: 0, left: 0, right: 0 };
        }
        const info = wx.getSystemInfoSync();
        const windowWidth = info.windowWidth || info.screenWidth;
        const windowHeight = info.windowHeight || info.screenHeight;
        const ratio = designScale(windowWidth);
        const safe = info.safeArea;
        if (!safe) {
            return { top: (info.statusBarHeight || 0) * ratio, bottom: 0, left: 0, right: 0 };
        }
        return {
            top: safe.top * ratio,
            bottom: Math.max(0, windowHeight - safe.bottom) * ratio,
            left: safe.left * ratio,
            right: Math.max(0, windowWidth - safe.right) * ratio,
        };
    }

    static getTopPadding(): number {
        if (!hasWx() || typeof wx.getMenuButtonBoundingClientRect !== 'function') {
            return SafeAreaAdapter.getSafeAreaInsets().top;
        }
        try {
            const info = wx.getSystemInfoSync();
            const rect = wx.getMenuButtonBoundingClientRect();
            const ratio = designScale(info.windowWidth || info.screenWidth);
            const gap = rect.top - (info.statusBarHeight || 0);
            return (rect.bottom + gap) * ratio;
        } catch (error) {
            return SafeAreaAdapter.getSafeAreaInsets().top;
        }
    }

    static applySafePadding(node: Node, options: Partial<SafeInsets>): Widget {
        const widgetOptions: WidgetOptions = {};
        if (options.top != null) {
            widgetOptions.top = options.top;
        }
        if (options.bottom != null) {
            widgetOptions.bottom = options.bottom;
        }
        if (options.left != null) {
            widgetOptions.left = options.left;
        }
        if (options.right != null) {
            widgetOptions.right = options.right;
        }
        return LayoutHelper.attachWidget(node, widgetOptions);
    }
}
