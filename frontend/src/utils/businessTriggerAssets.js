function resolveAssetDepartmentId(asset) {
  if (asset?.departmentId != null) {
    return Number(asset.departmentId);
  }
  if (asset?.department?.id != null) {
    return Number(asset.department.id);
  }
  return null;
}

/**
 * Returns assets belonging to the user's department for business trigger creation.
 */
export function filterAssetsByUserDepartment(assets, departmentId) {
  if (!Array.isArray(assets) || departmentId == null || departmentId === '') {
    return [];
  }

  const userDepartmentId = Number(departmentId);
  if (Number.isNaN(userDepartmentId)) {
    return [];
  }

  return assets.filter((asset) => {
    const assetDepartmentId = resolveAssetDepartmentId(asset);
    return assetDepartmentId != null && assetDepartmentId === userDepartmentId;
  });
}

export function resolveUserDepartmentId(authUser, profile) {
  const departmentId = authUser?.departmentId ?? profile?.departmentId;
  if (departmentId == null || departmentId === '') {
    return null;
  }
  return departmentId;
}
