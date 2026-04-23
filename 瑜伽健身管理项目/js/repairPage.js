new Vue({
  el: '#app',
  data() {
    return {
      activeIndex: '2-1',
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
      equipmentList: [],
      
      addOrderDialog: false,
      newOrder: {
        repairNotes: '',
        equipmentName: ''
      },
      submitting: false,
      
      updateOrderDialog: false,
      currentOrder: {
        repairId: '',
        repairNotes: '',
        equipName: '',
        images: []
      },
      updateFiles: [],
      updating: false,
      
      detailDialogVisible: false,
      currentDetail: {
        images: []
      },
      
      uploadHeaders: {
        'Authorization': `Bearer ${Cookies.get('token') || ''}`
      },
      
      previewVisible: false,
      previewImages: [],
      previewIndex: 0,
      
      refreshingImages: false,
      customUploadDisabled: false
    };
  },
  
  mounted() {
    this.fetchEquipment();
  },
  
  created() {
    if (!Cookies.get('userInfo') || localStorage.getItem('userInfo') === 'null') {
      this.$alert('你需要在登录状态下才可以访问此页面，点击确定将自动跳转到登录页面', '当前你未登录', {
        confirmButtonText: '确定',
        callback: () => window.location.href = '/page/login.html'
      });
    } else {
      const userInfo = JSON.parse(decodeURIComponent(Cookies.get('userInfo')));
      this.userId = userInfo.userId || this.userId;
      this.userRole = userInfo.userRole || this.userRole;
    }
    this.fetchTableData();
  },
  
  methods: {
    updateSearch(value) {
      this.searchKeyword = value;
    },

    filterTable() {
      this.pageNum = 1;
      this.fetchTableData();
    },

    fetchTableData() {
      this.loading = true;
      axios.get('http://127.0.0.1:8081/LX/repair/getAllRepairManagement', {
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
        if (response.data.code === 200) {
          if (response.data.data && response.data.data.repairRequest) {
            this.tableData = response.data.data.repairRequest.map(item => ({
              repairId: item.repairId,
              repairRequestId: item.repairRequestId,
              equipName: item.equipName,
              repairNotes: item.repairNotes,
              repairstatus: item.repairStatus,
              userID: item.userId,
              createdAt: item.createdAt,
              imageCount: item.imageCount || 0
            }));
            this.count = response.data.data.count;
          } else {
            this.tableData = response.data.data || [];
            this.count = response.data.data ? response.data.data.length : 0;
          }
        } else {
          this.$message.error('获取数据失败: ' + (response.data.msg || '未知错误'));
        }
      })
      .catch(error => {
        console.error('获取数据失败:', error);
        this.$message.error('获取数据失败，请稍后再试。');
      })
      .finally(() => {
        this.loading = false;
      });
    },

    viewOrderDetails(order) {
      this.currentDetail = { 
        ...order,
        images: []
      };
      this.loadRepairImages(order.repairId, 'detail');
      this.detailDialogVisible = true;
    },
    
    // 修复图片加载方法
    loadRepairImages(repairId, type = 'detail') {
      console.log(`开始加载图片，repairId: ${repairId}, type: ${type}`);
      
      axios.get(`http://127.0.0.1:8081/LX/repair/images/${repairId}`, {
        params: {
          _t: new Date().getTime()
        }
      })
      .then(response => {
        console.log(`图片加载响应 (${type}):`, response.data);
        
        if (response.data.code === 200) {
          let images = [];
          
          // 直接从data获取图片列表
          if (Array.isArray(response.data.data)) {
            images = response.data.data;
          }
          
          console.log(`获取到 ${images.length} 张图片`, images);
          
          // 处理图片URL
          const processedImages = images.map(img => {
            // 根据RepairImage对象的字段名获取数据
            return {
              imageId: img.imageId || img.id,
              imageUrl: img.imageUrl,
              imageName: img.imageName || '图片',
              _raw: img // 保留原始数据用于调试
            };
          }).filter(img => img.imageUrl); // 过滤掉没有URL的图片
          
          console.log(`处理后的 ${processedImages.length} 张图片:`, processedImages);
          
          if (type === 'detail') {
            this.currentDetail.images = processedImages;
          } else if (type === 'update') {
            this.currentOrder.images = processedImages;
          }
          
          // 如果没有图片，显示提示
          if (processedImages.length === 0) {
            console.log('没有找到可显示的图片');
          }
        } else {
          console.warn(`图片加载失败: ${response.data.msg}`);
        }
      })
      .catch(error => {
        console.error(`获取图片失败 (${type}):`, error);
        this.$message.warning('加载图片失败');
      });
    },
    
    closeDetailDialog() {
      this.currentDetail = {
        images: []
      };
      this.detailDialogVisible = false;
    },

    updateOrder(order) {
      this.currentOrder = {
        repairId: order.repairId,
        repairNotes: order.repairNotes,
        equipName: order.equipName,
        userId: order.userID || this.userId,
        images: []
      };
      this.updateFiles = [];
      this.loadRepairImages(order.repairId, 'update');
      this.updateOrderDialog = true;
    },
    
    closeUpdateDialog() {
      this.currentOrder = {
        repairId: '',
        repairNotes: '',
        equipName: '',
        images: []
      };
      this.updateFiles = [];
      this.updateOrderDialog = false;
    },

    submitUpdatedOrder() {
      if (!this.currentOrder.repairId) {
        this.$message.error('订单ID缺失，无法提交修改！');
        return;
      }
      
      this.updating = true;
      
      const params = new URLSearchParams();
      params.append('repairId', this.currentOrder.repairId);
      params.append('repairNotes', this.currentOrder.repairNotes);
      params.append('userId', this.userId);
      
      axios.post('http://127.0.0.1:8081/LX/repair/updateRepairManagement', params, {
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded'
        }
      })
      .then((response) => {
        if (response.data.code === 200) {
          this.$message.success('故障修改成功！');
          this.fetchTableData();
          this.updateOrderDialog = false;
        } else {
          this.$message.error(`修改失败：${response.data.msg}`);
        }
      })
      .catch((error) => {
        console.error('修改订单失败:', error);
        this.$message.error(`修改订单失败：${error.response?.data?.msg || error.message}`);
      })
      .finally(() => {
        this.updating = false;
      });
    },
    
    removeExistingImage(imageId, index) {
      if (!imageId) {
        this.currentOrder.images.splice(index, 1);
        this.$message.success('图片已移除');
        return;
      }
      
      this.$confirm('确定要删除这张图片吗？', '删除确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        axios.delete(`http://127.0.0.1:8081/LX/repair/image/${imageId}?userId=${this.userId}`)
        .then(response => {
          if (response.data.code === 200) {
            this.currentOrder.images.splice(index, 1);
            this.$message.success('图片删除成功');
          } else {
            this.$message.error(response.data.msg || '删除失败');
          }
        })
        .catch(error => {
          console.error('删除图片失败:', error);
          this.$message.error('删除失败，请稍后重试');
        });
      }).catch(() => {
        this.$message.info('取消删除操作');
      });
    },

    deleteOrder(order) {
      if (!order || !order.repairId) {
        this.$message.error('订单信息错误，无法删除！');
        return;
      }
      
      this.$prompt('请输入密码以确认删除订单', '删除确认', {
        inputType: 'password',
        confirmButtonText: '确定',
        cancelButtonText: '取消',
      })
      .then(({ value }) => {
        const password = value;
        if (!password) {
          this.$message.error('密码不能为空！');
          return;
        }
        
        const params = new URLSearchParams();
        params.append('repairId', order.repairId);
        params.append('userId', this.userId);
        params.append('password', password);
        
        axios.post('http://127.0.0.1:8081/LX/repair/deleteRepairManagement', params, {
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
          }
        })
        .then((response) => {
          if (response.data.code === 200) {
            this.$message.success('故障删除成功！');
            this.fetchTableData();
          } else {
            this.$message.error(`删除失败：${response.data.msg}`);
          }
        })
        .catch((error) => {
          console.error('删除订单失败:', error);
          this.$message.error('删除订单失败，请稍后再试。');
        });
      })
      .catch(() => {
        this.$message.info('已取消删除操作');
      });
    },

    handleSortChange({ prop, order }) {
      this.sortField = prop;
      this.sortOrder = order === 'ascending' ? 'asc' : 'desc';
      this.fetchTableData();
    },

    handleSizeChange(val) {
      this.pageSize = val;
      this.fetchTableData();
    },

    handleCurrentChange(val) {
      this.pageNum = val;
      this.fetchTableData();
    },

    addOrder() {
      this.newOrder = {
        repairNotes: '',
        equipmentName: '',
      };
      this.addOrderDialog = true;
    },
    
    closeAddDialog() {
      this.newOrder = {
        repairNotes: '',
        equipmentName: ''
      };
      this.addOrderDialog = false;
    },
    
    submitNewOrder() {
      if (!this.newOrder.repairNotes || !this.newOrder.equipmentName) {
        this.$message.error('请填写完整信息！');
        return;
      }
      
      this.submitting = true;
      
      const data = {
        repairNotes: this.newOrder.repairNotes,
        equipName: this.newOrder.equipmentName,
        userId: this.userId
      };
      
      axios.post('http://127.0.0.1:8081/LX/repair/createRepairManagement', data, {
        headers: {
          'Content-Type': 'application/json'
        }
      })
      .then((response) => {
        if (response.data.code === 200) {
          this.$message.success('故障申报成功！');
          this.fetchTableData();
          this.addOrderDialog = false;
        } else {
          this.$message.error(response.data.msg || '申报失败');
        }
      })
      .catch((error) => {
        console.error('添加故障失败:', error);
        this.$message.error('故障申报失败：' + (error.response?.data?.msg || '请检查网络连接'));
      })
      .finally(() => {
        this.submitting = false;
      });
    },

    fetchEquipment() {
      axios.get('http://127.0.0.1:8081/LX/repair/getAllEquipment')
        .then(response => {
          if (response.data && response.data.code === 200) {
            const list = response.data.data || [];
            this.equipmentList = Array.isArray(list) ? list.map(item => ({
              id: item.equipId,
              name: item.equipName
            })) : [];
          } else {
            this.equipmentList = [];
          }
        })
        .catch(() => {
          this.equipmentList = [
            { id: '1', name: '跑步机' },
            { id: '2', name: '动感单车' },
            { id: '3', name: '哑铃架' },
            { id: '4', name: '史密斯机' },
            { id: '5', name: '椭圆机' },
            { id: '6', name: '瑜伽垫架' },
            { id: '7', name: '力量训练器' }
          ];
        });
    },
    
    beforeUpload(file) {
      const isImage = file.type.startsWith('image/');
      const isLt5M = file.size / 1024 / 1024 < 5;
      
      if (!isImage) {
        this.$message.error('只能上传图片文件');
        return false;
      }
      if (!isLt5M) {
        this.$message.error('图片大小不能超过5MB');
        return false;
      }
      return true;
    },
    
    // 自定义上传方法
    customUpload(file) {
      if (this.customUploadDisabled) {
        this.$message.warning('正在上传中，请稍候...');
        return false;
      }
      
      if (!this.currentOrder.repairId) {
        this.$message.error('请先保存维修单信息');
        return false;
      }
      
      this.customUploadDisabled = true;
      
      const formData = new FormData();
      formData.append('files', file); // 参数名必须是 'files'
      formData.append('repairId', this.currentOrder.repairId);
      
      console.log('开始上传图片...', {
        repairId: this.currentOrder.repairId,
        fileName: file.name,
        fileSize: file.size
      });
      
      axios.post('http://127.0.0.1:8081/LX/repair/uploadImages', formData, {
        headers: {
          'Authorization': this.uploadHeaders.Authorization,
          'Content-Type': 'multipart/form-data'
        },
        timeout: 30000
      })
      .then(response => {
        console.log('上传响应:', response.data);
        
        if (response.data.code === 200) {
          // 由于后端返回的是字符串，我们需要解析它
          let resultData = response.data.data;
          console.log('上传结果数据:', resultData);
          
          // 尝试解析字符串为JSON对象
          try {
            if (typeof resultData === 'string') {
              // 尝试解析字符串
              const parsedData = JSON.parse(resultData);
              console.log('解析后的上传结果:', parsedData);
            }
          } catch (e) {
            console.log('无法解析上传结果字符串:', e);
          }
          
          // 无论返回什么，都尝试重新加载图片
          setTimeout(() => {
            this.loadRepairImages(this.currentOrder.repairId, 'update');
          }, 1000);
          
          this.$message.success('图片上传成功');
        } else {
          this.$message.error(response.data.msg || '上传失败');
        }
      })
      .catch(error => {
        console.error('上传失败:', error);
        this.$message.error('上传失败: ' + (error.message || '未知错误'));
      })
      .finally(() => {
        this.customUploadDisabled = false;
      });
      
      return false;
    },
    
    handleUpdateUploadSuccess(response, file, fileList) {
      console.log('Element UI 上传成功回调:', response);
      if (response.code === 200) {
        this.updateFiles = fileList;
        this.$message.success('图片上传成功');
        
        // 重新加载图片列表
        setTimeout(() => {
          if (this.currentOrder.repairId) {
            this.loadRepairImages(this.currentOrder.repairId, 'update');
          }
        }, 500);
      } else {
        this.$message.error(response.msg || '图片上传失败');
      }
    },
    
    handleUploadError(err, file, fileList) {
      console.error('上传错误:', err);
      this.$message.error('图片上传失败，请检查网络或文件大小');
    },
    
    handleExceed(files, fileList) {
      this.$message.warning('最多只能上传6张图片');
    },
    
    previewImage(images, index) {
      this.previewImages = images.map(img => img.imageUrl);
      this.previewIndex = index;
      this.previewVisible = true;
    },
    
    closePreview() {
      this.previewVisible = false;
      this.previewImages = [];
      this.previewIndex = 0;
    },
    
    prevImage() {
      this.previewIndex = this.previewIndex > 0 ? this.previewIndex - 1 : this.previewImages.length - 1;
    },
    
    nextImage() {
      this.previewIndex = this.previewIndex < this.previewImages.length - 1 ? this.previewIndex + 1 : 0;
    },
    
    refreshImages() {
      if (this.currentOrder.repairId) {
        this.refreshingImages = true;
        this.loadRepairImages(this.currentOrder.repairId, 'update');
        setTimeout(() => {
          this.refreshingImages = false;
        }, 1000);
      } else {
        this.$message.warning('没有维修单ID，无法刷新图片');
      }
    },
    
    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      });
    },
    
    getStatusTagType(status) {
      switch (status) {
        case '待处理':
          return 'warning';
        case '处理中':
          return 'primary';
        case '已完成':
          return 'success';
        case '已取消':
          return 'info';
        default:
          return 'default';
      }
    },

    goToIndex() { window.location.href = '/index.html'; },
    goToRepair() { window.location.href = '/page/repair.html'; },
    goToAccess() { window.location.href = '/page/access.html'; },
    goToUser() { window.location.href = '/page/user.html'; },
    goTosupplier() { window.location.href = '/page/supplier.html'; },

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
  },
});