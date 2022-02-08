<a href="https://www.youtube.com/watch?v=ojLrp2rmh0I
" target="_blank"><img src="http://img.youtube.com/vi/ojLrp2rmh0I/0.jpg" 
alt="IMAGE ALT TEXT HERE" width="240" height="180" border="10" /></a>

## Reference
1. [Vitepress official docs](https://vitepress.vuejs.org/)
2. [markdown-it](https://markdown-it.github.io/)
3. [Guthub vitepress docs Example](https://github.com/vuejs/vitepress/tree/master/docs)
4. [Guthub markdown cheatsheet](https://github.com/adam-p/markdown-here/wiki/Markdown-Cheatsheet)
5. [Markdown basic syntax](https://www.markdownguide.org/basic-syntax/)
6. [Markdown extended syntax](https://www.markdownguide.org/extended-syntax/)
7. [A list of emojis](https://github.com/markdown-it/markdown-it-emoji/blob/master/lib/data/full.json)
7. [A list of Code syntax highlighting](https://prismjs.com/#languages-list)

## Routing!


<!-- [docs/index.md](/) -> /

[docs/guide/one.md](/guide/getting-start) -> /one

[docs/api/index.md](/api/) -> /api/

[docs/guide/two.md](/guide/two) -> /guide/two -->

### All these options work!

<!-- [docs/guide/one.md](/guide/getting-start) | 
[docs/api/index.md](/api/) |
[docs/guide/two.md](/guide/two) -->

# Hello VitePress
| Tables        | Are           | Cool  |
| ------------- |:-------------:| -----:|
| col 3 is      | right-aligned | $1600 |
| col 2 is      | centered      |   $12 |
| zebra stripes | are neat      |    $1 

| Headings      | Are           | Centered    |
| ------------- |:-------------:| -----:      |
| left align    | centered      | right align |
| zebra striped | rows          | easy        |

### Blogging Like a Hacker
---

head:
  - - meta
    - name: description
      content: hello
  - - meta
    - name: keywords
      content: super duper SEO

### Emoji
---
:tada: :100:

## Custom Containers

::: tip
This is a tip
:::

::: warning
This is a warning
:::

::: danger
This is a dangerous warning
:::

::: danger STOP
Danger zone, do not proceed
:::

---
```js
export default {
  name: 'MyComponent',
  // ...
}
```

```html
<ul>
  <li v-for="todo in todos" :key="todo.id">
    {{ todo.text }}
  </li>
</ul>
```

```json{5}
{
    "role": {
        "name": "develop",
        "expiredAt": "2021-05-01",
        "description": "Highlighted!",
        "status": true
    }
}
```

```js{4}
export default {
  data () {
    return {
      msg: 'Highlighted!'
    }
  }
}
```

```js{1,4,6-7}
export default { // Highlighted
  data () {
    return {
      msg: `Highlighted!
      This line isn't highlighted,
      but this and the next 2 are.`,
      motd: 'VitePress is awesome',
      lorem: 'ipsum',
    }
  }
}
```

### Docs

::: v-pre
`{{ This will be displayed as-is }}`
:::

### text `<Tag/>`

---
head:
  - - meta
    - name: description
      content: hello
  - - meta
    - name: keywords
      content: super duper SEO
---