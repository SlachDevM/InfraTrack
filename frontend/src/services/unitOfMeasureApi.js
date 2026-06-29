import apiClient from './apiClient';

const ENDPOINT = '/api/units-of-measure';

export const unitOfMeasureApi = {
  list: (params = {}) => {
    const query = new URLSearchParams();
    if (params.active != null) {
      query.set('active', String(params.active));
    }
    if (params.quantityType) {
      query.set('quantityType', params.quantityType);
    }
    const suffix = query.toString() ? `?${query.toString()}` : '';
    return apiClient.get(`${ENDPOINT}${suffix}`);
  },
};

export default unitOfMeasureApi;
