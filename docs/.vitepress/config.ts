import Unocss from 'unocss/vite'
import {defineConfig} from 'vitepress'
import {version} from '../../package.json'
import {SearchPlugin} from 'vitepress-plugin-search'

export default defineConfig({
    lang: 'en-US',
    title: 'MongoCamp Server',
    description: 'Vite & Vue powered static site generator.',

    lastUpdated: true,

    themeConfig: {
        logo: '/logo_without_text.png',

        nav: nav(),

        sidebar: {
            '/guide/': sidebarGuide(),
            '/config/': sidebarConfig(),
            '/plugins/': sidebarPlugins()
        },

        editLink: {
            pattern: 'https://github.com/MongoCamp/mongocamp-server/edit/main/docs/:path',
            text: 'Edit this page on GitHub'
        },

        socialLinks: [
            { icon: 'github', link: 'https://github.com/MongoCamp/mongocamp-server' }
        ],

        footer: {
            message: 'Released under the Apache License 2.0.',
            copyright: 'Copyright © 2023 - MongoCamp Team'
        },

    },
    vite: {
        plugins: [
            Unocss({
                configFile: '../../unocss.config.ts',
            }),
            SearchPlugin(),
        ],
    },

})

function nav() {
    return [
        { text: 'Guide', link: '/guide/', activeMatch: '/guide/' },
        { text: 'Config', link: '/config/', activeMatch: '/config/' },
        { text: 'Plugins', link: '/plugins/', activeMatch: '/plugins/' },
        { text: 'REST API', link: '/rest/README', activeMatch: '/rest/' },
        {
            text: version,
            items: [
                {
                    text: 'Changelog',
                    link: '/changelog.html'
                },
            ],
        },

    ]
}

function sidebarGuide() {
    return [
        {
            text: 'Introduction',
            collapsible: true,
            items: [
                { text: 'About', link: '/guide/' },
                { text: 'Getting Started', link: '/guide/getting-started' },
                // { text: 'Configuration', link: '/guide/configuration' },
            ]
        },

    ]
}

function sidebarConfig() {
    return [
        {
            text: 'Config',
            items: [
                {text: 'Introduction', link: '/config/'},
                {text: 'Environment Config', link: '/config/environment'},
                {text: 'DB or Environment', link: '/config/environment-db'},
            ]
        }
    ]
}
function sidebarPlugins() {
    return [
        {
            text: 'Plugins',
            items: [
                {text: 'Introduction', link: '/plugins/'},
                {text: 'List of Plugins', link: '/plugins/list'},
                {
                    text: 'Development',
                    link: '/plugins/development/',
                    items: [
                        {
                            text: 'Server Plugin',
                            link: '/plugins/development/plugin-server'
                        },
                        {
                            text: 'Routes Plugin',
                            link: '/plugins/development/plugin-routes'
                        },
                        {
                            text: 'Files Plugin',
                            link: '/plugins/development/plugin-files'
                        },
                        {
                            text: 'Events',
                            link: '/plugins/events/'
                        },
                    ],
                },
            ]
        }
    ]
}
