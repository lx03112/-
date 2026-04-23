new Vue({
  el: '#app',
  data() {
    return {
      activeIndex: '5-1',
      searchKeyword: '',
      tableData: [],
      loading: false,
      userId: 1,
      userRole: 1,
      pageNum: 1,
      pageSize: 10,
      sortField: 'created_at',
      sortOrder: 'desc',
      count: 0,
      baseURL: 'http://127.0.0.1:8081',
      
      // 新增：用户个人信息
      userInfo: {},
      // 新增：升级弹窗相关
      upgradeDialogVisible: false,
      verificationCode: '',
      verifying: false,
      
      // 修改弹窗相关
      updateSupplierDialog: false,
      currentSupplier: {},
    };
  },
  created() {
    this.getUserInfoFromCookie();
  },
  methods: {
    getUserInfoFromCookie() {
      const userInfoEncoded = Cookies.get('userInfo');
      if (userInfoEncoded) {
        try {
          const userInfo = JSON.parse(decodeURIComponent(userInfoEncoded));
          this.userId = userInfo.userId || 1;
          this.userRole = userInfo.userRole || 1;
          // 获取用户详细信息
          this.fetchUserInfo();
        } catch (error) {
          console.error('解析用户信息失败:', error);
          this.$alert('登录信息异常，请重新登录', '错误', {
            confirmButtonText: '确定',
            callback: () => window.location.href = '/page/login.html'
          });
        }
      } else {
        this.$alert('未检测到登录状态，将跳转至登录页', '登录失效', {
          confirmButtonText: '确定',
          callback: () => window.location.href = '/page/login.html'
        });
      }
    },
    
    // 新增：获取用户个人信息
    fetchUserInfo() {
      axios.get(`${this.baseURL}/LX/user/getUserInfo`, {
        params: {
          userId: this.userId
        }
      })
      .then(response => {
        if (response.data.code === 200) {
          this.userInfo = response.data.data || {};
          console.log('用户信息:', this.userInfo);
          
          // 判断用户角色，如果是普通用户(4)，提示升级
          if (this.userInfo.roleId === 4) {
            setTimeout(() => {
              this.promptUpgrade();
            }, 500); // 延迟500ms显示，让页面先加载完成
          }
        } else {
          this.$message.error('获取用户信息失败：' + response.data.msg);
        }
      })
      .catch(error => {
        console.error('获取用户信息失败:', error);
        this.$message.error('获取用户信息失败，请稍后再试');
      });
    },
    
    // 新增：提示升级
    promptUpgrade() {
      this.$confirm('你还不是黄金用户，是否成为黄金用户？', '升级提示', {
        confirmButtonText: '立即升级',
        cancelButtonText: '暂不需要',
        type: 'warning'
      }).then(() => {
        // 用户点击确定，打开验证码输入对话框
        this.upgradeDialogVisible = true;
        this.verificationCode = '';
      }).catch(() => {
        // 用户点击取消
        this.$message.info('你可以随时在个人页面升级为黄金用户');
      });
    },
    
    // 新增：验证并升级
    handleUpgrade() {
      if (!this.verificationCode.trim()) {
        this.$message.warning('请输入验证码');
        return;
      }
      
      this.verifying = true;
      axios.post(`${this.baseURL}/LX/user/upgradeToGold`, {
        userId: this.userId,
        verificationCode: this.verificationCode.trim()
      })
      .then(response => {
        if (response.data.code === 200) {
          this.$message.success('升级成功！您现在是黄金用户了');
          this.upgradeDialogVisible = false;
          // 刷新用户信息
          this.fetchUserInfo();
          // 更新Cookie中的用户角色信息
          this.updateCookieRole(5);
        } else {
          this.$message.error('升级失败：' + response.data.msg);
        }
      })
      .catch(error => {
        console.error('升级失败:', error);
        const errorMsg = error.response?.data?.msg || error.message || '网络错误';
        this.$message.error('升级失败：' + errorMsg);
      })
      .finally(() => {
        this.verifying = false;
      });
    },
    
    // 新增：更新Cookie中的角色信息
    updateCookieRole(newRoleId) {
      const userInfoEncoded = Cookies.get('userInfo');
      if (userInfoEncoded) {
        try {
          const userInfo = JSON.parse(decodeURIComponent(userInfoEncoded));
          userInfo.userRole = newRoleId;
          Cookies.set('userInfo', encodeURIComponent(JSON.stringify(userInfo)), { expires: 7 });
        } catch (error) {
          console.error('更新Cookie失败:', error);
        }
      }
    },
    
    // 获取角色名称
    getRoleName(roleId) {
      const roleMap = {
        1: '管理员',
        2: '前台接待员', 
        3: '健身教练',
        4: '普通用户',
        5: '黄金用户',
        6: '财务'
      };
      return roleMap[roleId] || '未知角色';
    },
    
    // 获取用户状态文本
    getUserStatusText(status) {
      return status === 'active' ? '活跃' : '已封禁';
    },
    
    // 格式化日期
    formatDate(dateStr) {
      if (!dateStr) return '';
      return new Date(dateStr).toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      });
    },

    goTosupplier() { 
      window.location.href = '/page/supplier.html'; 
    },

    updateSearch(value) {
      this.searchKeyword = value;
    },
    
    filterTable() {
      this.pageNum = 1;
      this.fetchTableData();
    },

    fetchTableData() {
      this.loading = true;
      axios.get(`${this.baseURL}/supplier/getAllSupplierManagement`, {
        params: {
          searchKeyword: this.searchKeyword,
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          sortField: this.sortField,
          sortOrder: this.sortOrder
        }
      })
      .then(response => {
        if (response.data.code === 200) {
          const resData = response.data.data || {};
          if (!resData.supplierManagementList) {
            this.$message.error('数据格式错误，请联系开发人员');
            return;
          }
          this.tableData = resData.supplierManagementList.map(item => ({
            supplierManagementId: item.supplierManagementId || '',
            supplierName: item.supplierName || '',
            supplierId: item.supplierId || '',
            partId: item.partId || '',
            partName: item.partName || '',
            supplyQuantity: item.supplyQuantity || 0,
            createdAt: item.createdAt || ''
          }));
          this.count = resData.count || 0;
        } else {
          this.$message.error(`获取数据失败：${response.data.msg || '未知错误'}`);
        }
      })
      .catch(() => {
        this.$message.error('获取数据失败，请稍后再试');
      })
      .finally(() => {
        this.loading = false;
      });
    },

    goToPage(pageName) {
      window.location.href = `/${pageName}.html`;
    },
    
    goToIndex() { this.goToPage('index'); },
    goToRepair() { this.goToPage('page/repair'); },
    goToAccess() { this.goToPage('page/access'); },
    goToUser() { this.goToPage('page/user'); },
    goToSupplier() { this.goToPage('page/supplier'); },

    logout() {
      this.$confirm('确定要退出登录吗？', '退出确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      .then(() => {
        Cookies.remove('userInfo');
        this.goToPage('page/login');
      })
      .catch(() => {
        this.$message.info('已取消退出操作');
      });
    }
  }
});