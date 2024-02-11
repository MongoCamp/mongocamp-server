import DefaultTheme from 'vitepress/theme'
import './custom.css'
import type { App } from 'vue'
import { anu } from 'anu-vue'
import 'uno.css'
import DependencyGroup from './components/DependencyGroup.vue'

export default {
    ...DefaultTheme,
    enhanceApp({app}: { app: App }) {
        app.use(anu, {
            registerComponents: true,
        })
        app.component('DependencyGroup', DependencyGroup)
    }
}

