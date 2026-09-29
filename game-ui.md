*   请先阅读一下两个个链接的文档，再进行设计

    1.  <https://developers.weixin.qq.com/minigame/dev/guide/>
    2.  <https://docs.cocos.com/creator/4.0/manual/zh/editor/publish/publish-wechatgame.html>

*   请使用 Cocos Creator 3.8 TypeScript 实现微信小游戏的屏幕适配。

    要求：

    1.  设计分辨率设为 750×1334（竖屏基准）；
    2.  Canvas 组件勾选 Align Canvas With Screen；
    3.  Canvas Layer 设为 UI\_2D；
    4.  Widget 组件 Align Mode 设为 ON\_WINDOW\_RESIZE，四边偏移均为0；
    5.  场景 Tone Mapping 设为 LINEAR；
    6.  根据屏幕宽高比动态切换 Fit Height / Fit Width 模式： - 当屏幕宽高比 < 设计分辨率宽高比时，启用 Fit Height - 当屏幕宽高比 > 设计分辨率宽高比时，启用 Fit Width；
    7.  不要使用浏览器 DOM API（document/window），全部使用 Cocos 3.x API。

*   用 Cocos Creator 3.8 TypeScript 写一个屏幕适配管理器 ScreenAdapter.ts。

    功能要求：

    1.  单例模式，全局唯一实例；
    2.  设计分辨率常量：750 × 1334；
    3.  onLoad 中获取 Canvas 组件，根据屏幕实际宽高比动态设置 fitHeight / fitWidth： - 计算 screenRatio = view\.getVisibleSize().width / view\.getVisibleSize().height - 计算 designRatio = 750 / 1334 - screenRatio < designRatio → fitHeight = true, fitWidth = false - screenRatio > designRatio → fitHeight = false, fitWidth = true；
    4.  监听 view\.resize 事件，窗口变化时重新计算；
    5.  提供 getSafeArea() 方法，通过 wx.getSystemInfoSync() 获取安全区域；
    6.  提供 getTopSafePadding() 方法，通过 wx.getMenuButtonBoundingClientRect() 计算顶部胶囊按钮下方的安全间距；
    7.  兼容微信小游戏环境，使用 wx API 而非浏览器 API。

*   用 Cocos Creator 3.8 TypeScript 写一个 UI 自适应布局工具类 LayoutHelper.ts。

    功能要求：

    1.  提供 attachWidget(node, options) 方法，给指定节点添加 Widget 组件；
    2.  options 参数包含：top/bottom/left/right（边距）、alignMode（对齐模式）、horizontal/vertical（对齐方式）；
    3.  提供 pinToTop(node, padding) → 将节点锚定到顶部，距离顶部 padding 像素；
    4.  提供 pinToBottom(node, padding) → 将节点锚定到底部；
    5.  提供 pinToLeft(node, padding) → 将节点锚定到左侧；
    6.  提供 pinToRight(node, padding) → 将节点锚定到右侧；
    7.  提供 centerInParent(node) → 将节点居中于父节点；
    8.  所有 Widget 的 alignMode 默认使用 ON\_WINDOW\_RESIZE 9. 使用 cc.Widget 和 cc.Alignment 等 Cocos 3.x API。

*   用 Cocos Creator 3.8 TypeScript 实现微信小游戏的安全区域适配。

    要求：

    1.  创建 SafeAreaAdapter.ts 工具类；
    2.  提供 getSafeAreaInsets() 方法： - 调用 wx.getSystemInfoSync() 获取 statusBarHeight - 调用 wx.getMenuButtonBoundingClientRect() 获取胶囊按钮位置 - 计算顶部安全间距 = 胶囊按钮 bottom + (胶囊按钮 top - statusBarHeight) - 返回 { top, bottom, left, right } 四个方向的安全边距；
    3.  提供 applySafePadding(node, options) 方法： - 给节点添加 Widget 组件 - 根据 options 中的 top/bottom/left/right 设置对应边距 - alignMode 设为 ON\_WINDOW\_RESIZE；
    4.  特别注意：不要用 CSS 的 env(safe-area-inset-\*)，微信小游戏不支持；
    5.  单位统一使用 Cocos 的逻辑像素，不要做额外的 DPI 转换

*   用 Cocos Creator 3.8 TypeScript 实现背景图的全屏自适应。

    要求：

    1.  背景图节点尺寸设为 Canvas 的设计分辨率（750×1334）；
    2.  使用 Widget 组件，四边偏移设为 0，alignMode 为 ON\_WINDOW\_RESIZE；
    3.  背景图的 Sprite 组件 Type 设为 SLICED 或 TILED，确保拉伸不变形；
    4.  如果背景图是纯色或渐变，使用 Graphics 组件绘制，避免图片拉伸模糊；å
    5.  提供 setBackground(node) 工具方法，自动完成上述配置

*   用 Cocos Creator 3.8 TypeScript 实现一个桌游策略类微信小游戏的 UI 布局。

    游戏场景要求：

    1.  设计分辨率 750×1334，竖屏；
    2.  顶部区域（高度120）：显示当前天数场景，使用 Widget 锚定到顶部；
    3.  中部区域（高度900）：角色展示，使用 Layout 组件垂直排列，自动均分空间；
    4.  底部区域（高度314）：技能按钮（给刀/投票/上警/警徽流/查杀/毒药/解药/开枪），使用 Layout 组件水平排列，自动均分宽度；底部按钮下方展示流程提示文本。
    5.  所有 UI 节点使用 Widget 组件 + ON\_WINDOW\_RESIZE 模式；
    6.  底部按钮区域需要避开微信底部安全区，调用 wx.getSystemInfoSync() 获取 safeArea.bottom 并设置底部边距；
    7.  顶部区域需要避开胶囊按钮，调用 wx.getMenuButtonBoundingClientRect() 获取位置并设置顶部边距；
    8.  使用 Cocos 3.x API，不要使用浏览器 DOM

