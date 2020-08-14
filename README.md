# SamrtScrollBar
Android ScrollBar,自定义性极强的滚动条。

## 导入项目

**Step 1.** 添加 jitpack 仓库：
```kotlin
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```

**Step 2.** 添加依赖
```kotlin
dependencies {
	implementation 'com.github.cathu:SamrtScrollBar:0.0.4'
}
```

## 已支持和未支持的功能
```xml
    <declare-styleable name="SmartScrollBar">
        <!-- ✅  方向 -->
        <attr name="smart_orientation" format="enum">
            <enum name="horizontal" value="0"/>
            <enum name="vertical" value="1"/>
        </attr>
        <!-- ✅  background 圆角（只支持 Android 5.0 以上的版本） -->
        <attr name="smart_background_corner" format="dimension"/>
        <!-- ✅  sliderBar 圆角 -->
        <attr name="smart_slider_corner" format="dimension"/>
        <!-- ✅  sliderBar 颜色 -->
        <attr name="smart_slider_color" format="color"/>
        <!-- ✅  RecyclerView 无法滑动时，scroll 的状态 -->
        <attr name="smart_cant_scroll_style" format="enum">
            <enum name="dismiss_invisible" value="0"/>  <!-- 消失（invisible） -->
            <enum name="dismiss_gone" value="1"/>   <!-- 消失（gone） -->
            <enum name="visible" value="2"/>    <!-- 显示 scroll -->
        </attr>
        <!-- ✅ 滑块风格 -->
        <attr name="smart_slider_style" format="enum">
            <enum name="倍数缩放" value="0"/>
            <enum name="固定大小" value="1"/>
        </attr>
        <!-- ✅  滑块长度 -->
        <attr name="smart_slider_length" format="fraction|dimension"/>
        <!-- ❌  滑块图片 -->
        <attr name="smart_slider_img" format="reference"/>
        <!-- ✅  滑块隐藏风格 -->
        <attr name="smart_can_scroll_style" format="enum">
            <enum name="always_show" value="0"/>
            <enum name="dismiss_awhile" value="1"/>
        </attr>
        <!-- ✅  sliderBar 多少毫秒(ms)后消失 -->
        <attr name="smart_dismiss_time" format="integer"/>
        <!-- ✅ 支持拖拽 sliderBar -->
        <attr name="smart_enable_drag" format="boolean" />
    </declare-styleable>
```
