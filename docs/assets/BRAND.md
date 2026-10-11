# ext-library 品牌设计指南

## 设计理念

ext-library 的视觉设计围绕三个核心概念：

1. **扩展性 (Extension)** - "EXT" 作为核心标识，代表扩展和增强
2. **模块化 (Modularity)** - 方块元素象征独立可组合的模块
3. **现代感 (Modern)** - 渐变色和圆角设计体现现代技术栈

---

## 色彩系统

### 主色调

| 名称 | 色值 | 用途 |
|------|------|------|
| Primary | `#6366f1` | 主要品牌色 |
| Primary Dark | `#4f46e5` | 深色变体 |
| Accent | `#22d3ee` | 强调色/高亮 |

### 渐变

```css
/* 主渐变 */
background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);

/* 强调渐变 */
background: linear-gradient(135deg, #22d3ee 0%, #06b6d4 100%);
```

### 深色主题背景

| 名称 | 色值 | 用途 |
|------|------|------|
| Background | `#0f172a` | 主背景 |
| Card | `#1e293b` | 卡片背景 |
| Border | `#334155` | 边框 |
| Text | `#e2e8f0` | 主文字 |
| Text Muted | `#94a3b8` | 次要文字 |

---

## 图标规范

### 主 Logo (`logo.svg`)

- **尺寸**: 200x200px
- **圆角**: 40px
- **用途**: 网站、文档、社交媒体

```
┌────────────────────┐
│  ▪           ▪     │  ← 装饰方块 (模块化)
│                    │
│       EXT          │  ← 核心文字
│       ───          │  ← 下划线装饰
│                    │
│  ▪             ●───│  ← 连接线 (扩展性)
└────────────────────┘
```

### 深色 Logo (`logo-dark.svg`)

- 深紫色背景 + 发光 EXT 文字
- 适用于深色背景场景

### Favicon (`favicon.svg`)

- **尺寸**: 64x64px
- **设计**: 简化版，仅保留 EXT 文字
- **用途**: 浏览器标签页、书签

---

## Banner 设计

启动横幅 (`banner.txt`) 使用 ANSI 彩色输出：

```
    ███████╗██╗  ██╗████████╗
    ██╔════╝╚██╗██╔╝╚══██╔══╝   (青色)
    █████╗   ╚███╔╝    ██║
    ██╔══╝   ██╔██╗    ██║
    ███████╗██╔╝ ██╗   ██║
    ╚══════╝╚═╝  ╚═╝   ╚═╝      LIBRARY (绿色)
```

### 颜色映射

| 元素 | ANSI 颜色 |
|------|-----------|
| EXT 文字 | `BRIGHT_CYAN` |
| LIBRARY | `BRIGHT_GREEN` |
| 信息框 | `WHITE` |
| 特性标签 | `CYAN` |
| 版本号 | `GREEN` |
| Emoji | 各色 |

---

## 文件清单

```
docs/
├── assets/
│   ├── logo.svg          # 主 Logo (亮色)
│   ├── logo-dark.svg     # 深色 Logo
│   ├── favicon.svg       # Favicon
│   └── BRAND.md          # 本文档
└── index.html            # 项目介绍页
```

---

## 使用指南

### 引用 Logo

```html
<!-- 在 HTML 中 -->
<img src="docs/assets/logo.svg" alt="ext-library" width="64">

<!-- 在 Markdown 中 -->
![ext-library](docs/assets/logo.svg)
```

### GitHub Badge

可配合 shields.io 使用自定义 logo：

```markdown
![ext-library](https://img.shields.io/badge/ext--library-4.0.0-6366f1?logo=data:image/svg+xml;base64,...)
```

---

## 设计工具推荐

- **图标编辑**: [Figma](https://figma.com), [Inkscape](https://inkscape.org)
- **ASCII Art**: [patorjk.com](http://patorjk.com/software/taag/)
- **颜色工具**: [Tailwind CSS Colors](https://tailwindcss.com/docs/colors)

---

*Last Updated: 2026-01-21*
