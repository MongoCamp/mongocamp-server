module.exports = {
    title: "MongoCamp Server",
    base: "/mongocamp/",
    themeConfig: {
        repo: 'mongocamp/mongocamp-server',
        docsDir: 'docs',
        docsBranch: 'main',
        editLinks: true,
        editLinkText: 'Edit this page on GitHub',
        lastUpdated: 'Last Updated',
        nav: [
            {text: 'Guide', link: '/guide/', activeMatch: '^/$|^/guide/'},
            {
                text: 'Quick Start',
                link: '/quickstart/#setup',
                activeMatch: '^/quickstart/'
            },
            {
                text: 'REST API',
                link: '/rest/README',
                activeMatch: '^/rest/'
            },
            {
                text: 'mongodb-driver',
                link: 'https://mongocamp.github.io/mongodb-driver/'
            }
        ],// appended to all page titles
    }
};
