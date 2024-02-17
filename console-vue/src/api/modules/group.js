import http from '../axios'
export default {
  // 查询分组集合
  queryGroup(data) {
    return http({
      url: '/groups',
      method: 'get',
      params: data
    })
  },
  // 新增短链分组
  addGroup(data) {
    return http({
      url: '/groups',
      method: 'post',
      data
    })
  },
  // 修改短链分组
  editGroup(data) {
    return http({
      url: '/groups',
      method: 'put',
      data
    })
  },
  // 删除短链分组
  deleteGroup(data) {
    return http({
      url: '/groups',
      method: 'delete',
      params: data
    })
  },
  sortGroup(data) {
    return http({
      url: '/groups/sort',
      method: 'post',
      data
    })
  },
  // 查询分组的图表数据
  queryGroupStats(data) {
    return http({
      method: 'get',
      params: data,
      url: 'stats/group'
    })
  },
  // 查询分组的访问记录
  queryGroupTable(data) {
    return http({
      method: 'post',
      data,
      url: 'stats/access-record/group'
    })
  }
}