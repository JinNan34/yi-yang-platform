/**
 * 与分页配套：当前查询结果内的连续序号（跨页，从 1 起）。
 * @param {import('vue').Ref<number>} pageRef 当前页码（1 起）
 * @param {number | import('vue').Ref<number>} pageSize 每页条数
 */
export function createRowIndexGetter(pageRef, pageSize) {
  const getSize = typeof pageSize === 'number' ? () => pageSize : () => pageSize.value
  return (i) => (pageRef.value - 1) * getSize() + i + 1
}
