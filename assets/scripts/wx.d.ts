interface WxRect {
    width: number;
    height: number;
    top: number;
    bottom: number;
    left: number;
    right: number;
}

interface WxSystemInfo {
    statusBarHeight: number;
    screenWidth: number;
    screenHeight: number;
    windowWidth: number;
    windowHeight: number;
    safeArea?: WxRect;
}

interface WxSocketTask {
    onOpen(callback: () => void): void;
    onMessage(callback: (res: { data: string | ArrayBuffer }) => void): void;
    onError(callback: (error: { errMsg?: string }) => void): void;
    onClose(callback: () => void): void;
    send(options: { data: string }): void;
    close(options?: { code?: number; reason?: string }): void;
}

declare const wx: {
    getSystemInfoSync(): WxSystemInfo;
    getMenuButtonBoundingClientRect(): WxRect;
    connectSocket(options: { url: string }): WxSocketTask;
};
