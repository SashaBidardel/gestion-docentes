<template>
  <v-app>
    <NavigationMenu v-if="!$route.meta.isPublic" />

    <v-main class="bg-grey-lighten-3">
      <v-container fluid class="pa-0 fill-height align-start">
        <router-view></router-view>
      </v-container>
    </v-main>

    <AppFooter v-if="!$route.meta.isPublic" />
  </v-app>
</template>

<script setup>
import NavigationMenu from './components/NavigationMenu.vue'
import AppFooter from './components/AppFooter.vue'
</script>