<template>
  <router-view />
</template>

<script setup>
</script>

<style>
/* ===== 全局设计 - 浅色主题 ===== */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

:root {
  --primary-color: #06b6d4;
  --primary-light: #0ec5e6;
  --primary-dark: #0891b2;
  --primary-gradient: linear-gradient(135deg, #06b6d4 0%, #0891b2 50%, #0e7490 100%);
  --success-color: #10b981;
  --warning-color: #f59e0b;
  --danger-color: #f43f5e;
  --info-color: #6366f1;
  --bg-primary: #f0f2f5;
  --bg-secondary: #ffffff;
  --bg-tertiary: #fafafa;
  --bg-card: #ffffff;
  --bg-elevated: #ffffff;
  --border-color: #e4e7ed;
  --border-light: #ebeef5;
  --text-primary: #303133;
  --text-secondary: #606266;
  --text-muted: #909399;
  --shadow-sm: 0 1px 3px rgba(0,0,0,0.06);
  --shadow-md: 0 4px 20px rgba(0,0,0,0.08);
  --shadow-lg: 0 8px 40px rgba(0,0,0,0.1);
  --radius-sm: 8px;
  --radius-md: 12px;
  --radius-lg: 16px;
  --radius-xl: 20px;
  --transition-fast: 0.2s cubic-bezier(0.4,0,0.2,1);
  --transition-normal: 0.3s cubic-bezier(0.4,0,0.2,1);
}

html, body, #app {
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, "Noto Sans SC", sans-serif;
  background: var(--bg-primary);
  color: var(--text-primary);
}

/* Element Plus 浅色覆盖 */
.el-card {
  background: var(--bg-card) !important;
  border: 1px solid var(--border-color) !important;
  border-radius: var(--radius-lg) !important;
  box-shadow: var(--shadow-sm) !important;
}
.el-card:hover {
  border-color: rgba(6,182,212,0.3) !important;
  box-shadow: var(--shadow-md) !important;
}
.el-card__header {
  border-bottom: 1px solid var(--border-color) !important;
  padding: 18px 24px !important;
}

.el-table {
  background: var(--bg-secondary) !important;
  color: var(--text-primary) !important;
}
.el-table th.el-table__cell {
  background: #f5f7fa !important;
  color: var(--text-primary) !important;
  font-weight: 600 !important;
  border-bottom: 1px solid var(--border-color) !important;
}
.el-table__cell {
  background: var(--bg-secondary) !important;
  color: var(--text-secondary) !important;
  border-bottom: 1px solid var(--border-light) !important;
}
.el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell {
  background: #fafafa !important;
}
.el-table--enable-row-hover .el-table__body tr:hover > td.el-table__cell {
  background: #ecf5ff !important;
}

.el-input__wrapper, .el-select__wrapper, .el-textarea__inner {
  background: var(--bg-secondary) !important;
  border: 1px solid #dcdfe6 !important;
  border-radius: var(--radius-sm) !important;
  box-shadow: none !important;
}
.el-input__wrapper:hover, .el-select__wrapper:hover, .el-textarea__inner:hover {
  border-color: var(--primary-dark) !important;
}
.el-input__wrapper.is-focus, .el-select__wrapper.is-focus {
  border-color: var(--primary-color) !important;
  box-shadow: 0 0 0 3px rgba(6,182,212,0.1) !important;
}
.el-input__inner, .el-select__placeholder, .el-textarea__inner {
  color: var(--text-primary) !important;
}
.el-form-item__label {
  color: var(--text-secondary) !important;
  font-weight: 500 !important;
}

.el-button {
  border-radius: var(--radius-sm) !important;
  font-weight: 600 !important;
}
.el-button--primary {
  background: var(--primary-gradient) !important;
  border: none !important;
}
.el-button--primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 20px rgba(6,182,212,0.3) !important;
}
.el-button--danger {
  background: linear-gradient(135deg, #f43f5e, #e11d48) !important;
  border: none !important;
}
.el-button--success {
  background: linear-gradient(135deg, #10b981, #059669) !important;
  border: none !important;
}
.el-button--warning {
  background: linear-gradient(135deg, #f59e0b, #d97706) !important;
  border: none !important;
}

.el-tag { border: none !important; font-weight: 500 !important; border-radius: 6px !important; }
.el-tag--success { background: rgba(16,185,129,0.1) !important; color: #059669 !important; }
.el-tag--danger { background: rgba(244,63,94,0.1) !important; color: #dc2626 !important; }
.el-tag--warning { background: rgba(245,158,11,0.1) !important; color: #d97706 !important; }
.el-tag--info { background: rgba(100,116,139,0.1) !important; color: #6b7280 !important; }

.el-dialog {
  background: var(--bg-secondary) !important;
  border: 1px solid var(--border-color) !important;
  border-radius: var(--radius-xl) !important;
  box-shadow: var(--shadow-lg) !important;
}
.el-dialog__header { border-bottom: 1px solid var(--border-color) !important; padding: 20px 24px !important; }
.el-dialog__title { color: var(--text-primary) !important; font-weight: 700 !important; }
.el-dialog__body { padding: 24px !important; }
.el-dialog__footer { border-top: 1px solid var(--border-color) !important; padding: 16px 24px !important; }

.el-dropdown-menu {
  background: var(--bg-secondary) !important;
  border: 1px solid var(--border-color) !important;
  border-radius: var(--radius-md) !important;
  box-shadow: var(--shadow-lg) !important;
}
.el-dropdown-menu__item { color: var(--text-secondary) !important; }
.el-dropdown-menu__item:hover { background: #ecf5ff !important; color: var(--primary-color) !important; }

.el-message {
  background: var(--bg-elevated) !important;
  border: 1px solid var(--border-color) !important;
  border-radius: var(--radius-md) !important;
  color: var(--text-primary) !important;
}

.el-pagination { --el-pagination-text-color: var(--text-secondary); --el-pagination-hover-color: var(--primary-color); }
.el-pagination button, .el-pager li { background: transparent !important; color: var(--text-secondary) !important; border-radius: var(--radius-sm) !important; }
.el-pager li.active { background: var(--primary-gradient) !important; color: #fff !important; }

.el-empty__description p { color: var(--text-muted) !important; }

::-webkit-scrollbar { width: 6px; height: 6px; }
::-webkit-scrollbar-track { background: transparent; }
::-webkit-scrollbar-thumb { background: rgba(0,0,0,0.1); border-radius: 3px; }

.fade-enter-active, .fade-leave-active { transition: opacity var(--transition-normal); }
.fade-enter-from, .fade-leave-to { opacity: 0; }
.slide-fade-enter-active { transition: all var(--transition-normal); }
.slide-fade-leave-active { transition: all var(--transition-fast); }
.slide-fade-enter-from { transform: translateY(10px); opacity: 0; }
.slide-fade-leave-to { opacity: 0; }
</style>
