# SamrtScrollBar
[![](https://jitpack.io/v/cathu/SamrtScrollBar.svg)](https://jitpack.io/#cathu/SamrtScrollBar)
[![](https://img.shields.io/badge/%E5%8D%9A%E5%AE%A2-Cathu-brightgreen)](https://blog.csdn.net/catzifeng)

Android ScrollBar,自定义性极强的滚动条。

## 效果展示
<img src="https://github.com/cathu/SamrtScrollBar/blob/master/screen/screen-shot-01.gif" width="300"/><br/>

## 导入项目

**Step 1.** 添加 jitpack 仓库：
```kotlin
allprojects {
	repositories {
		...
		maven { url 'https://www.jitpack.io' }
	}
}
```

**Step 2.** 添加依赖
```kotlin
dependencies {
	implementation 'com.github.cathu:SamrtScrollBar:0.1.1'
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

```xml
<com.cathu.core.SmartScrollBar
        android:id="@+id/scrollBar"
        android:layout_width="40dp"
        android:layout_height="200dp"
        android:background="#ACACAC"
        android:layout_marginBottom="45dp"
        app:smart_can_scroll_style="always_show"
        app:smart_cant_scroll_style="dismiss_invisible"
        app:smart_orientation="vertical"
        app:smart_slider_color="#494949"
        app:smart_background_corner="10dp"
        app:smart_slider_corner="10dp"
        app:smart_slider_style="固定大小"
        app:smart_slider_length="50%"	<!-- 支持百分比/dp -->
        app:smart_enable_drag="true"/>
```

支持自定义背景：
```kotlin
scrollBar.setCustomBackground(holderWH: (Int,Int) -> RectF, corner: Float, @ColorInt color: Int)
```

```holderWH``` 会返回宽高，利用宽高你可以创建一个 RectF 对象并返回给 SmartScrollBar，他会构建一个长方形背景。利用这个你可以实现背景的宽度比滑块的宽度小的场景。

