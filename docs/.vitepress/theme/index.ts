import DefaultTheme from 'vitepress/theme'
import type { Theme } from 'vitepress'
import './custom.css'
import DependencyGroup from './components/DependencyGroup.vue'

export default {
    extends: DefaultTheme,
    enhanceApp({app}) {
        app.component('DependencyGroup', DependencyGroup)
    }
} satisfies Theme
