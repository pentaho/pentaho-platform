## Bi App LESS
bi-app lets you write your stylesheets once, and have them compiled into 2 different stylesheets one for `left-to-right` layout, and the other for `right-to-left` layouts 

## Credits
created by [Anas Nakawa](//twitter.com/anasnakawa),  
inspired by [Victor Zamfir](//twitter.com/victorzamfir)

## License
[MIT License](http://www.opensource.org/licenses/mit-license.php)

## Why
usually when writing stylesheets for bi-directional sites/apps, both `ltr` & `rtl` stylesheets mostly will look the same, except for direction related properties (`float, text-align, padding, margin ..etc` ), so when you write a `float: left` in some `ltr` stylesheet, you'll have to write it again as `float: right` for the `rtl` one

when using **bi-app-less** , all you have to do is to write your stylesheets once using a predefined mixins for those direction related properties, and once you compile your stylesheets, you'll have a ready two stylesheets for your bi-directional app

## How to use it
create three less files
```js
app-ltr.less    // ltr interface to be compiled
app-rtl.less    // rtl interface
app.less        // private file where you will write your styles (don't compile this one)
```
in the `app-ltr.less` only include the following
```css
@import 'bi-app-ltr';
@import 'app';
```

same for `app-rtl.less`
```css
@import 'bi-app-rtl';
@import 'app';
```

now you can write your styles in `_app.less`, using bi-app mixins, as you were styling for only `ltr` layouts, and the `rtl` styles will be compiled automatically!
```css
.foo {
  display: block;
  .float(left);
  .border-left(1px solid white);
  ...
}
```

the result will be ..

in `app-ltr.css`
```css
.foo {
  display: block;
  float: left;
  border-left: 1px solid white;
  ...
}
```

in `app-rtl.css`
```css
.foo {
  display: block;
  float: right;
  border-right: 1px solid white;
  ...
}
```

## Installing via Bower
```
bower install bi-app-less
```

## Installing via Yeoman
```
yeoman install bi-app-less
```

## Reference
a list of available mixins for CSS properties
```
// padding
padding-left(distance)
padding-right(distance)
padding(top, right, bottom, left)

// margin
margin-left(distance)
margin-right(distance)
margin(top, right, bottom, left)

// float
float(direction)			// left || right || none

// text align
text-align(direction)		// left || right || center

// clear
clear(direction)			// left || right || both

// left / right
left(distance)
right(distance)

// border
border-left(border-style)
border-right(border-style)

// border width
border-left-width(width)
border-right-width(width)
border-width(top, right, bottom, left)

// border style
border-left-style(style)
border-right-style(style)
border-style(top, right, bottom, left)

// border color
border-left-color(color)
border-right-color(color)
border-color(top, right, bottom, left)

// border radius (soon..)
border-top-left-radius(radius)
border-top-right-radius(radius)
border-bottom-left-radius(radius)
border-bottom-right-radius(radius)
border-left-radius(radius)
border-right-radius(radius)
border-top-radius(radius)
border-bottom-radius(radius)
border-radius(topLeft, topRight, bottomRight, bottomLeft)
```