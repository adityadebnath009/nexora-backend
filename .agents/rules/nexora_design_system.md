# Rule: Nexora Design System & Frontend Engineering Standards

This document serves as the single source of truth for every frontend implementation in Nexora. Every page, component, email template, dashboard, modal, animation, and UI element must follow these standards to maintain a consistent, premium developer experience.

## Core Principles
* Developer-first
* AI-native
* Performance-first
* Accessibility-first
* Mobile-first
* Consistency over creativity
* Reusable over duplicated
* Modern but timeless

## 1. Brand Identity & Vision
* **Vision**: AI-powered professional developer network where contributions and verified skills matter more than resumes.
* **Tone & Feel**: Premium, technical, trustworthy, fast, minimal, intelligent.
* **Inspirations**: Linear, GitHub, Vercel, Raycast, Cursor (Avoid traditional corporate dashboards).

## 2. Design Principles
* **Simplicity**: Every element must have a purpose. Avoid unnecessary decorations.
* **Hierarchy**: Users must immediately distinguish primary actions, secondary actions, and supporting details.
* **Accessibility**: Full keyboard support, focus outlines, semantic HTML, and ARIA labels. Target WCAG AA compliance.

## 3. Colour System
* **Core Theme**:
  - `background`: `#0B0F19` (canvas)
  - `surface`: `#161B26` (cards, inputs, modals)
  - `surface-hover`: `#1C2433`
  - `surface-elevated`: `#20293B`
  - `border`: `#2A3347` (default components)
  - `border-hover`: `#3A4763`
  - `active`: `#3B82F6` (active outlines, focus)
  - `accent`: `#4F8CFF` (links, switches)
  - `overlay`: `rgba(11,15,25,0.75)`
* **Typography Colours**:
  - `text-primary`: `#E2E8F0`
  - `text-secondary`: `#94A3B8`
  - `text-muted`: `#64748B`
  - `text-disabled`: `#475569`
  - `code`: `#38BDF8`
* **Semantic Colours**:
  - `success`: `#10B981` | `warning`: `#F59E0B` | `danger`: `#EF4444` | `info`: `#3B82F6`
* **AI Status Colours**:
  - AI Verified: `#10B981` | Human Verified: `#3B82F6` | AI Generated: `#8B5CF6` | Needs Review: `#F59E0B` | Expired: `#EF4444` | Unknown: `#64748B`

## 4. Brand Gradients (Linear 135deg)
* **Primary**: `#3B82F6` to `#8B5CF6` (primary actions, hero, highlights)
* **Cyber**: `#06B6D4` to `#3B82F6` (AI widgets, analytics, visualizations)
* **Success**: `#10B981` to `#06B6D4` (verification badges, achievements, roadmap success)

## 5. Typography Specs
* **Primary / Alt Font**: Inter / Outfit (Fallback: `system-ui, sans-serif`)
* **Monospace Font**: JetBrains Mono (Fallback: `Fira Code, monospace`)
  * *Strict Monospace Rule*: Only use monospace for: IDs, XP, scores, badges, reputation numbers, timestamps, commit hashes, and AI confidence percentages. Never use monospace for body copy/paragraphs.

## 6. Typography Scale
* Hero (56px) | H1 (40px) | H2 (32px) | H3 (24px) | H4 (20px) | H5 (18px) | Body Large (18px) | Body (16px) | Small (14px) | Caption (12px)
* Line Heights: Headings (120%) | Body text (160%)

## 7. Spacing System
Only use these values: `4` | `8` | `12` | `16` | `20` | `24` | `32` | `40` | `48` | `64` | `80` | `96`

## 8. Border Radii
* `radius-xs` (4px) | `radius-sm` (8px) | `radius-md` (12px) | `radius-lg` (16px) | `radius-xl` (20px) | `radius-pill` (9999px)

## 9. Elevation Stacking
* Background &rarr; Cards &rarr; Dialogs &rarr; Floating Components &rarr; Notifications (Depth comes from borders, colors, and shadows, not just blur)

## 10. Shadow Tokens
* Small: `0 2px 8px rgba(0,0,0,.15)`
* Medium: `0 8px 24px rgba(0,0,0,.20)`
* Large: `0 16px 48px rgba(0,0,0,.30)`
* Glow: `0 0 30px rgba(59,130,246,.25)` (restricted to AI, primary actions, and verified states)

## 11. Motion Tokens
* Hover (150ms) | Default (250ms) | Modal (400ms) | Easing (`ease-out`, `ease-in-out`).

## 12. Component States & Standards
* Every component must define: default, hover, active, focus, loading, disabled, error, success, empty, and skeleton states.
* Core Stack: React, Vite, TS, Tailwind CSS v4, react-router-dom, Zustand, TanStack Query, Axios, React Hook Form, Zod, lucide-react, framer-motion, Recharts, Sonner, date-fns, react-markdown, shiki, @tanstack/react-virtual.

## 13. API Layer Standards
* Axios must handle automatic authorization header injection, silent refresh token rotation on 401, retry failed once, and centralized error parsing.

## 14. Non-Negotiable Rules
* Never use random colors, invent spacing/radii values, use multiple icon/animation libraries, use Bootstrap/MUI components, hardcode colors, ignore accessibility, or duplicate API logic.
