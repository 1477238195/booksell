import Request from '@/utils/request'

const RecommendApi = {
  homeBooks(params) {
    return Request({
      url: '/api/recommend/home/books',
      method: 'get',
      params
    })
  },

  homeWanted(params) {
    return Request({
      url: '/api/recommend/home/wanted',
      method: 'get',
      params
    })
  }
}

export default RecommendApi
