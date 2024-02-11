<script>
export default {
  props: ['organization', 'name', 'version']
}

</script>

<template>
    <div class="language-plugin">
        <button title="Copy Code" class="copy"></button>
        <span class="lang">plugin configuration</span>
        <pre class="shiki material-theme-palenight"><code><span class="line"><span style="color:#A6ACCD;">{{ organization }}:{{ name }}_2.13:{{ version }}</span></span></code></pre>
    </div>
</template>

<style scoped>
</style>