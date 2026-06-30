export function isTrendSeriesEmpty(dataPoints) {
  if (!dataPoints?.length) {
    return true;
  }
  return dataPoints.every((point) => !point.count);
}

export function getTrendMaxCount(dataPoints) {
  if (!dataPoints?.length) {
    return 0;
  }
  return Math.max(...dataPoints.map((point) => point.count ?? 0));
}
