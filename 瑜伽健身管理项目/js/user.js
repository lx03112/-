new Vue({
    el: '#app',
    data() {
      return {
        activeIndex: '4-1',      // 当前导航选中的菜单项
        searchKeyword: '',
        tableData: [],         // 表格数据
        loading: false,        // 加载状态
        userId: null,             // 用户ID
        userRole: 1,           // 用户角色
        pageNum: 1,            // 当前页码
        pageSize: 10,          // 每页显示条数
        sortField: 'created_at', // 排序字段
        sortPart: 'desc',     // 排序顺序
        count: 0,              // 总记录数
        roles: [], // 新增：存储所有角色（ID + 名称）
        addUserDialog: false, // 是否显示添加用户的弹窗
        updateUserDialog: false, // 是否显示修改用户的弹窗
        currentUser: {}, // 当前被选中的用户数据
        newUser: {
          userName: '',
          userPwd:'',
          userEmail: '',
          userPhone:'',
        },
      };
    },
    created() {
      this.fetchAllRoles().then(() => {
        this.fetchTableData(); // 角色数据加载完成后再获取用户数据
      });
    },
    methods: {
      // 实时更新搜索关键词
      updateSearch(value) {
        this.searchKeyword = value;
        console.log(`查询来自: ${value}`);
      },

      // 搜索后端数据
      filterTable() {
        console.log(`查询来自: ${this.searchKeyword}`);
        this.fetchTableData();
      },

      // 获取表格数据，支持带参数的搜索
      fetchTableData() {
        this.loading = true;
        console.log('使用参数从后端获取表数据:', {
          userId: this.userId,
          userRole: this.userRole,
          searchKeyword: this.searchKeyword,
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          sortField: this.sortField,
        sortOrder: this.sortOrder,  // 使用 sortOrder
        });

        // 使用 Axios 发送请求到后端
        axios.get('http://127.0.0.1:8081/LX/user/getAllUsers', {
          params: {
            userId: this.userId,
            userRole: this.userRole,
            searchKeyword: this.searchKeyword,
            pageNum: this.pageNum,
            pageSize: this.pageSize,
            sortField: this.sortField,
           sortOrder: this.sortOrder,  // 使用 sortOrder
          }
        }).then(response => {
              console.log('后端返回的数据:', response.data);
              // 假设后端返回的数据在 response.data.repairRequest
              this.tableData = response.data.data.userList.map(item => ({
                userId: item.userId,
                userName: item.userName,
                userEmail: item.userEmail,
                roleId: item.roleId,
                userBio: item.userBio,
                userPhone: item.userPhone,
                userCreatedAt: item.userCreatedAt,
                userStatus: item.userStatus, // 新增
              }));
              this.count = response.data.data.count; // 总记录数
            })
            .catch(error => {
              console.error('获取表数据时出错:', error);
              this.$message.error('获取数据失败，请稍后再试。');
            })
            .finally(() => {
              this.loading = false;
            });
      },

      // 新增：获取所有角色
      fetchAllRoles() {
        return axios.get('http://127.0.0.1:8081/LX/user/list')
            .then(response => {
              console.log('后端返回的角色数据:', response.data);
              // 从 response.data 中提取 data 字段（角色数组）
              const roleData = response.data.data || [];
              // 确保 roleData 是数组
              this.roles = Array.isArray(roleData) ? roleData : [];
              // console.log('处理后的 roles 数组:', this.roles);
            })
            .catch(error => {
              console.error('获取角色列表失败:', error);
              this.$message.error('获取角色列表失败，请稍后再试');
              this.roles = [];
            });
      },
      // 辅助方法：根据 roleId 找角色名称（表格展示用）
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

      handleSelect(index) {
        this.showModel = parseInt(index); // 切换页面
      },

      handleSearch() {
        this.filterTable();
      },

      handleSortChange({ prop, user }) {
        // 传递排序参数到后端
        this.sortField = prop;
        this.sortPart = user === 'ascending' ? 'asc' : 'desc';
        this.fetchTableData();
      },

      viewUserDetails(user) {
        this.$alert(
          `<h3>用户详情：</h3>
          用户ID：${user.userId} <br>
          用户名称：${user.userName} <br>
          用户邮箱：${user.userEmail} <br>
          用户简介：${user.userBio} <br>
          用户角色：${user.roleId} <br>
          用户状态：${user.userStatus === 'active' ? '活跃' : '已封禁'} <br>
          用户手机号：${user.userPhone} <br>
          创建时间：${user.userCreatedAt}`,
          
          '用户详情',
          {
            dangerouslyUseHTMLString: true,
            confirmButtonText: '确定'
          }
        );
      },


      // 修改订单
      updateUser(user) {
        // 深拷贝订单数据，确保 user_id 被传递
        this.currentUser = {
          userId: user.userId,
          userName: user.userName ,
          userEmail: user.userEmail,
          roleId: user.roleId,
          userPhone :user.userPhone,
          userBio:user.userBio,
        };
        this.updateUserDialog = true; // 打开弹窗
      },

    submitUpdatedUser(row){
    const newRoleId = row.roleId === 4 ? 5 : 4;
    const action = row.roleId === 4 ? '升级为会员' : '降级为普通用户';
    
    this.$confirm(`确定要将该用户${action}吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(() => {
        axios.post('http://127.0.0.1:8081/LX/user/changeRole', {
            userId: row.userId,
            newRoleId: newRoleId
        }, {
            headers: { 'Content-Type': 'application/json' }
        }).then((response) => {
            if (response.data.code === 200) {
                this.$message.success(`${action}成功`);
                this.fetchTableData();
            } else {
                this.$message.error(`${action}失败：${response.data.message}`);
            }
        }).catch((error) => {
            this.$message.error(`${action}失败：${error.response?.data?.message || error.message}`);
        });
    }).catch(() => {});
},

     // 封禁用户
banUser(user) {
    this.$confirm(
        `确认封禁用户 ID ${user.userId} 吗？`,
        '封禁确认',
        {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
        }
    )
    .then(() => {
        // 调用后端封禁接口
        axios.post(`http://127.0.0.1:8081/LX/user/ban/${user.userId}`)
            .then((response) => {
                if (response.data.code === 200) {
                    this.$message.success('用户封禁成功！');
                    // 刷新表格数据
                    this.fetchTableData();
                } else {
                    this.$message.error(`封禁失败：${response.data.msg}`);
                }
            })
            .catch((error) => {
                this.$message.error(`封禁失败：${error.response?.data?.msg || error.message}`);
            });
    })
    .catch(() => {
        this.$message.info('取消封禁操作');
    });
},

// 解禁用户
unbanUser(user) {
    this.$confirm(
        `确认解禁用户 ID ${user.userId} 吗？`,
        '解禁确认',
        {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
        }
    )
    .then(() => {
        // 调用后端解禁接口
        axios.post(`http://127.0.0.1:8081/LX/user/unban/${user.userId}`)
            .then((response) => {
                if (response.data.code === 200) {
                    this.$message.success('用户解禁成功！');
                    // 刷新表格数据
                    this.fetchTableData();
                } else {
                    this.$message.error(`解禁失败：${response.data.msg}`);
                }
            })
            .catch((error) => {
                this.$message.error(`解禁失败：${error.response?.data?.msg || error.message}`);
            });
    })
    .catch(() => {
        this.$message.info('取消解禁操作');
    });
},

      logout() {
        this.$confirm('你确定要退出登录吗？', '退出确认', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        })
        .then(() => {
          Cookies.remove('userInfo');
          window.location.href = '/page/login.html';
        })
        .catch(() => {
          this.$message.info('已取消退出操作');
        });
      },

      // 分页组件方法
      handleSizeChange(val) {
        this.pageSize = val;
        this.fetchTableData();
      },
      handleCurrentChange(val) {
        this.pageNum = val;
        this.fetchTableData();
      },

      // 添加订单（逻辑占位）
      addUser() {
        // 打开添加订单的弹窗
        this.newUser = {
          userName: '',
          userPwd:'',
          userEmail: '',
          userPhone:'',
        }; // 重置表单数据
        this.addUserDialog = true;
      },

      submitNewUser() {
        if (!this.newUser.userName || !this.newUser.userEmail) {
          this.$message.error('请填写完整信息！');
          return;
        }
        const newUser = {
          userName: this.newUser.userName,
          userEmail: this.newUser.userEmail,
          userPwd:this.newUser.userPwd,
          userPhone:this.newUser.userPhone,
        };
        axios.post('http://127.0.0.1:8081/LX/user/createUser', newUser)
            .then(() => {
              this.$message.success('用户添加成功！');
              this.fetchTableData(); // 刷新表格数据
              this.addUserDialog = false; // 关闭弹窗
            })
            .catch((error) => {
              console.error('添加订单失败:', error);
              this.$message.error('添加订单失败，请稍后重试。');
            });
      },
      // 跳转的方法
      goToIndex() {window.location.href = '/index.html';},
      goToRepair() {window.location.href = '/page/repair.html';},
      goToAccess() {window.location.href = '/page/access.html';},
      goToUser() {window.location.href = '/page/user.html';},
      goTosupplier() {window.location.href = '/page/supplier.html';},
    },
  });
