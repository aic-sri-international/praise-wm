<template>
  <b-btn size="sm"
            variant="outline-secondary"
            :title="title"
            @click.stop="$emit('clicked')">New
    <i class="fas fa-plus-square" style="color: green"></i></b-btn>
</template>

<script>
  export default {
    name: 'newObjectButton',
    props: {
      title: {
        type: String,
      },
    },
  };
</script>