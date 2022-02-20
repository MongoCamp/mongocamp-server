module.exports = {
    title: "MongoCamp",
    base: "/mongocamp/",
    themeConfig: {
        repo: 'QuadStingray/mongocamp',
        docsDir: 'docs',
        docsBranch: 'main',
        editLinks: true,
        editLinkText: 'Edit this page on GitHub',
        lastUpdated: 'Last Updated',
        nav: [
            {text: 'Guide', link: '/guide/', activeMatch: '^/$|^/guide/'},
            {
                text: 'Config Reference',
                link: '/config/#setup',
                activeMatch: '^/config/'
            },
            {
                text: 'simple-mongo',
                link: 'https://sfxcode.github.io/simple-mongo'
            }
        ],// appended to all page titles
    }
};
