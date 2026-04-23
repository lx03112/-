new Vue({
  el: '#app',
  data() {
      return {
          activeIndex: '1-1',      // 当前导航选中的菜单项
          searchKeyword: '',     // 模糊查询关键词
          tableData: [],         // 表格数据
          loading: false,        // 加载状态
          userId: 0,             // 用户ID
          userRole: 0,           // 用户角色
          userPasswd: '',        // 用户密码
          pageNum: 1,            // 当前页码
          pageSize: 10,          // 每页显示条数
          sortField: 'created_at', // 排序字段
          sortOrder: 'desc',     // 排序顺序
          count: 0,              // 总记录数
          coaches: [],           // 所有的教练
          showAddOrderDialog: false, // 是否显示添加课程对话框
          newOrder: { // 新课程数据对象
              courseType: '',
              courseDescription: '',
              coachId: '',
          },
      };
  },
  created() {
      this.fetchTableData(); // 初始化获取表格数据
      if (!Cookies.get('userInfo') || localStorage.getItem('userInfo') === 'null') {
          this.$alert('你需要在登录状态下才可以访问此网站的内容，点击确定将自动跳转到登录页面', '当前你未登录', {
              confirmButtonText: '确定',
              callback: action => {
                  window.location.href = '/page/login.html';
              }
          });
      } else {
          const userInfo = JSON.parse(decodeURIComponent(Cookies.get('userInfo')));
          this.userId = userInfo.userId;
          this.userPasswd = userInfo.userPasswd;
      }
  },
  mounted() {
      this.fetchCoaches(); // 获取所有的教练
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
          // 从 Cookie 中获取用户信息
          const userInfoEncoded = Cookies.get('userInfo');
          if (userInfoEncoded) {
              const userInfoDecoded = decodeURIComponent(userInfoEncoded);
              const userInfo = JSON.parse(userInfoDecoded);

              this.userId = userInfo.userId;
              this.userRole = userInfo.userRole;

              console.log(`用户ID: ${this.userId}, 用户角色: ${this.userRole}`);
          }
          this.loading = true;
          console.log('使用参数从后端获取表数据:', {
              userId: this.userId,
              userRole: this.userRole,
              searchKeyword: this.searchKeyword,
              pageNum: this.pageNum,
              pageSize: this.pageSize,
              sortField: this.sortField,
              sortOrder: this.sortOrder,
          });

          // 使用 Axios 发送请求到后端
          axios.get('http://127.0.0.1:8081/LX/management/getAllCourseManagement', {
              params: {
                  userId: this.userId,
                  userRole: this.userRole,
                  searchKeyword: this.searchKeyword,
                  pageNum: this.pageNum,
                  pageSize: this.pageSize,
                  sortField: this.sortField,
                  sortOrder: this.sortOrder,
              }
          })
          .then(response => {
              console.log('后端返回的数据:', response.data);
              // 后端返回的数据在 response.data.data（Map对象，包含data数组和count）
              this.tableData = response.data.data.courseRequest.map(item => ({
                  courseId: item.courseId,
                  courseModel: item.courseModel,
                  courseDescription: item.courseDescription,
                  expectStatus: item.expectStatus,
                  coach: item.coach,        
                  time: item.timeSchedule, 
              }));
              this.count = response.data.count; // 总记录数
          })         
          .catch(error => {
              console.error('获取表数据时出错:', error);
              this.$message.error('获取数据失败，请稍后再试。');
          })
          .finally(() => {
              this.loading = false;
          });
      },

      handleSortChange({ prop, order }) {
          // 传递排序参数到后端
          this.sortField = prop;
          this.sortOrder = order === 'ascending' ? 'asc' : 'desc';
          this.fetchTableData();
      },

      viewOrderDetails(order) {
          this.$alert(
              `<h3>课程详情：</h3>
              课程ID：${order.courseId} <br>
              课程类型：${order.courseModel} <br>
              课程内容：${order.courseDescription} <br>
              预期成果：${order.expectStatus} <br>
              教练：${order.coach} <br>
              时间安排：${order.time}`,
              '课程详情',
              {
                  dangerouslyUseHTMLString: true,
                  confirmButtonText: '确定'
              }
          );
      },

      deleteOrder(order) {
          this.$prompt('请输入密码以确认删除', '删除确认', {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              inputType: 'password'
          }).then(({ value }) => {
              // 密码长度不能超过30位，并且不能包含空格和=等特殊字符
              if (value.length > 30 || /\s|=/.test(value)) {
                  this.$message.error('密码格式不正确！');
                  return;
              }
              // 调用后端删除接口
              axios.post('http://127.0.0.1:8081/LX/management/deleteCourseManagement', null, {
                  params: {
                      courseId: order.courseId,
                      userId: this.userId,
                      password: value
                  }
              })
              .then(response => {
                  if (response.data.code === 200) {
                      this.tableData = this.tableData.filter(o => o.courseId !== order.courseId);
                      this.$message.success('课程删除成功！');
                  } else {
                      console.error('courseId:', order.courseId);
                      console.error('密码:', value);
                      console.error('userid：', this.userId);
                      this.$message.error('删除课程失败，请稍后再试。');
                  }
              })
              .catch(() => {
                  this.$message.error('删除订单失败，请稍后再试。');
              });
          }).catch(() => {
              this.$message.info('取消删除操作');
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

      fetchCoaches() {
          // 发送 GET 请求到后端，获取所有的教练
          axios.get('http://127.0.0.1:8081/LX/management/getAllCoach')
              .then(response => {
                  if (response.data.code === 200) {
                      // 确保正确赋值
                      this.coaches = response.data.data.map(item => ({
                          userId: item.userId,
                          userName: item.userName
                      }));
                      console.log('教练列表:', this.coaches);
                  } else {
                      console.error('获取教练失败:', response.data.msg);
                      this.$message.error('获取教练失败，请稍后再试。');
                  }
              })
              .catch(error => {
                  console.error('获取教练时出错:', error);
                  this.$message.error('获取教练失败，请稍后再试。');
              });
      },

      // 添加课程的方法
      addOrder() {
          this.showAddOrderDialog = true;
      },
      // 提交课程
      submitNewOrder() {
          // 发送请求到后端保存课程
          axios.post('http://127.0.0.1:8081/LX/management/createCourseManagement', {
              userId: this.userId,
              coachId: this.newOrder.coachId,
              courseModel: this.newOrder.courseModel,
              courseDescription: this.newOrder.courseDescription,
              expectStatus: this.newOrder.expectStatus,
              timeSchedule: this.newOrder.timeSchedule
          })
          .then(response => {
              if (response.data.code === 200) {
                  this.$message.success('课程创建成功！');
                  this.showAddOrderDialog = false; // 关闭弹出框
                  this.fetchTableData(); // 更新表格数据
              } else {
                  this.$message.error('课程创建失败，请稍后再试。');
              }
          })
          .catch(error => {
              console.error('提交课程时出错:', error);
              this.$message.error('课程创建失败，请稍后再试。');
          });
      },

    // 跳转的方法
    goToIndex() { window.location.href = '/index.html'; },
    goToRepair() { window.location.href = '/page/repair.html'; },
    goToAccess() { window.location.href = '/page/access.html'; },
    goToUser() { window.location.href = '/page/user.html'; },
    goTosupplier() { window.location.href = '/page/supplier.html'; },
},
watch: {
  // 监听关键词是否超过一定数量
  searchKeyword(value) {
      if (value.length > 20) {
          this.$message.warning('搜索关键词过长！不能超过20字');
          this.searchKeyword = value.slice(0, 20);
      }
  },
  // 监听课程 问题描述是否超过一定数量
  'newOrder.courseType'(value) {
      if (value.length > 50) {
          this.$message.warning('输入字符过长！不能超过50字');
          this.newOrder.courseType = value.slice(0, 50);
      }
  },
  'newOrder.courseDescription'(value) {
      if (value.length > 200) {
          this.$message.warning('输入字符过长！不能超过200字');
          this.newOrder.courseDescription = value.slice(0, 200);
      }
  },
}
});