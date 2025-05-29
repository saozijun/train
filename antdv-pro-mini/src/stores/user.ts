import type { MenuData } from '~@/layouts/basic-layout/typing'
import dynamicRoutes, { rootRoute, getRoutesByRole } from '~@/router/dynamic-routes'
import { genRoutes } from '~@/router/generate-route'
import { updateInfo } from '~@/api/user'

export const useUserStore = defineStore('user', () => {
  const routerData = shallowRef()
  const menuData = shallowRef<MenuData>([])
  
  // 使用 useStorage 持久化存储用户信息
  const userInfo: any = useStorage('user-info', {})

  const avatar = computed(() => userInfo.value.avatarUrl && userInfo.value.avatarUrl != "" ? userInfo.value.avatarUrl : 'https://gw.alipayobjects.com/zos/rmsportal/BiazfanxmamNRoxxVxka.png')
  const nickname = computed(() => userInfo.value.nickname ?? userInfo.value.username)

  const generateRoutes = async () => {
    const role = userInfo.value.role || 'admin' // 获取用户角色
    const routes = getRoutesByRole(role) // 根据角色获取路由
    
    const currentRoute = {
      ...rootRoute,
      children: routes,
    }
    menuData.value = genRoutes(routes)
    return currentRoute
  }

  const generateDynamicRoutes = async () => {
    const routerDatas = await generateRoutes()
    routerData.value = routerDatas
    return routerDatas
  }

  // 更新用户信息
  const updateAvatar = async (url: any) => {
    userInfo.value.avatarUrl = url
    await updateInfo(userInfo.value)
  }

  // 添加清除用户信息的方法
  const clearUserInfo = () => {
    userInfo.value = {}
  }

  return {
    routerData,
    menuData,
    generateDynamicRoutes,
    avatar,
    nickname,
    userInfo,
    clearUserInfo,
    updateAvatar,
  }
})
