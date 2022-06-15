import { defineConfig } from 'vitepress'

export default defineConfig({
    lang: 'en-US',
    title: 'MongoCamp Server',
    description: 'Vite & Vue powered static site generator.',

    lastUpdated: true,

    themeConfig: {
        nav: nav(),

        sidebar: {
            '/guide/': sidebarGuide(),
            '/config/': sidebarConfig()
        },

        editLink: {
            pattern: 'https://github.com/MongoCamp/mongocamp-server/edit/main/docs/:path',
            text: 'Edit this page on GitHub'
        },

        socialLinks: [
            { icon: 'github', link: 'https://github.com/MongoCamp/mongocamp-server' }
        ],

        footer: {
            message: 'Released under the MIT License.',
            copyright: 'Copyright © 2022 - MongoCamp Team'
        },

    }
})

function nav() {
    return [
        { text: 'Guide', link: '/guide/', activeMatch: '/guide/' },
        { text: 'Config', link: '/config/', activeMatch: '/config/' },
        { text: 'REST API', link: '/rest/README', activeMatch: '/config/' },
        {
            text: 'Changelog',
            link: 'https://github.com/MongoCamp/mongocamp-server/blob/main/CHANGELOG.md'
        }
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
                { text: 'Configuration', link: '/guide/configuration' },
            ]
        },

    ]
}

function sidebarConfig() {
    return [
        {
            text: 'Config',
            items: [
                { text: 'Introduction', link: '/config/' },
                { text: 'Docker Config', link: '/config/docker' },
            ]
        }
    ]
}
