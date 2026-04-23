new Vue({
  el: '#app',
  data() {
    return {
      activeIndex: '3-1',      // 当前导航选中的菜单项
      searchKeyword: '',
      tableData: [],         // 表格数据
      loading: false,        // 加载状态
      userId: 1,             // 用户ID
      userRole: 1,           // 用户角色
      pageNum: 1,            // 当前页码
      pageSize: 10,          // 每页显示条数
      sortField: 'partId', // 排序字段
      sortPart: 'asc',     // 排序顺序
      count: 0,              // 总记录数
      addPartDialog: false, // 是否显示添加配件的弹窗
      updatePartDialog: false, // 是否显示修改配件的弹窗
      currentPart: {}, // 当前被选中的配件数据
      newPart: {
        partName: '',
        partPrice:'',
        stockQuantity: '',
        supplierId:''
      },
    };
  },
  created() {
    this.fetchTableData(); // 初始化获取表格数据
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
    //退出
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
    addPart() {
      // 打开添加订单的弹窗
      this.newPart = {
        partName: '',
        partPrice:'',
        stockQuantity: '',
        supplierId:'',
      }; // 重置表单数据
      this.addPartDialog = true;
    },

    submitNewPart() {
      if (!this.newPart.partName || !this.newPart.partPrice || !this.newPart.stockQuantity || !this.newPart.supplierId) {
        this.$message.error('请填写完整信息！');
        return;
      }
      const newPart = {
        partName: this.newPart.partName,
        partPrice: this.newPart.partPrice,
        stockQuantity:this.newPart.stockQuantity,
        supplierId:this.newPart.supplierId
      };
      axios.post('http://127.0.0.1:8081/yjx/parts/addPart', newPart)
          .then(() => {
            this.$message.success('配件添加成功！');
            this.fetchTableData(); // 刷新表格数据
            this.addPartDialog = false; // 关闭弹窗
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


// 等待DOM加载完成
document.addEventListener('DOMContentLoaded', function() {
    // 获取元素
    const banner = document.querySelector('.banner');
    const picContainer = banner.querySelector('.pic');
    const dots = banner.querySelectorAll('.dots li');
    const arrowLeft = banner.querySelector('.arrow.left');
    const arrowRight = banner.querySelector('.arrow.right');
    
    let currentIndex = 0; // 当前图片索引
    const totalImages = dots.length; // 图片总数
    
    // 切换到指定图片
    function goToSlide(index) {
        // 确保索引在有效范围内
        if (index < 0) index = totalImages - 1;
        if (index >= totalImages) index = 0;
        
        // 更新当前索引
        currentIndex = index;
        
        // 移动图片容器
        // 每张图片占25%宽度，所以移动的距离是 -index * 25%
        picContainer.style.transform = `translateX(-${currentIndex * 25}%)`;
        
        // 更新圆点状态
        dots.forEach((dot, i) => {
            if (i === currentIndex) {
                dot.classList.add('current');
            } else {
                dot.classList.remove('current');
            }
        });
    }
    
    // 为每个圆点添加点击事件
    dots.forEach((dot, index) => {
        dot.addEventListener('click', function() {
            goToSlide(index);
        });
    });
    
    // 左箭头点击事件 - 上一张
    if (arrowLeft) {
        arrowLeft.addEventListener('click', function() {
            goToSlide(currentIndex - 1);
        });
    }
    
    // 右箭头点击事件 - 下一张
    if (arrowRight) {
        arrowRight.addEventListener('click', function() {
            goToSlide(currentIndex + 1);
        });
    }
    
    // 自动轮播（可选）
    let autoSlideInterval;
    
    function startAutoSlide() {
        autoSlideInterval = setInterval(function() {
            goToSlide(currentIndex + 1);
        }, 3000); // 每3秒切换一次
    }
    
    function stopAutoSlide() {
        clearInterval(autoSlideInterval);
    }
    
    // 鼠标悬停时停止自动轮播，离开时继续
    banner.addEventListener('mouseenter', stopAutoSlide);
    banner.addEventListener('mouseleave', startAutoSlide);
    
    // 开始自动轮播
    startAutoSlide();
    
    // 键盘导航（可选）
    document.addEventListener('keydown', function(event) {
        if (event.key === 'ArrowLeft') {
            goToSlide(currentIndex - 1);
        } else if (event.key === 'ArrowRight') {
            goToSlide(currentIndex + 1);
        }
    });
});

const BACKEND_BASE = 'http://127.0.0.1:8081';
        const CONTEXT_PATH = '/LX';
        const API_PATH = '/equipment';
        
        // 完整的API基础URL
        const API_BASE_URL = `${BACKEND_BASE}${CONTEXT_PATH}${API_PATH}`;
        
        console.log('API配置:', {
            backendBase: BACKEND_BASE,
            contextPath: CONTEXT_PATH,
            apiPath: API_PATH,
            fullApiBase: API_BASE_URL
        });
        
        // 页面加载时初始化
        document.addEventListener('DOMContentLoaded', function() {
            console.log('页面初始化...');
            
            // 1. 先加载分类
            loadCategories();
            
            // 2. 然后加载热门器材
            setTimeout(() => {
                loadEquipments('热门');
            }, 100);
            
            // 3. 为"查看全部"按钮添加点击事件
            const viewAllBtn = document.querySelector('.access-right .more');
            if (viewAllBtn) {
                viewAllBtn.addEventListener('click', function(e) {
                    e.preventDefault();
                    loadAllEquipments();
                });
            }
        });
        
        // 加载所有分类
        async function loadCategories() {
            try {
                const response = await fetch(`${API_BASE_URL}/categories`);
                if (!response.ok) {
                    throw new Error(`获取分类失败: ${response.status}`);
                }
                
                const categories = await response.json();
                console.log('获取到的分类:', categories);
                
                renderCategoryList(categories);
            } catch (error) {
                console.error('加载分类失败:', error);
                showCategoryError();
            }
        }
        
        // 渲染分类列表
        function renderCategoryList(categories) {
            const categoryList = document.getElementById('categoryList');
            if (!categoryList) return;
            
            // 清空现有分类（保留前两个：热门和新品推荐）
            const existingItems = categoryList.querySelectorAll('li');
            for (let i = 2; i < existingItems.length; i++) {
                existingItems[i].remove();
            }
            
            // 添加从数据库获取的分类
            if (categories && categories.length > 0) {
                categories.forEach(category => {
                    if (category && category.trim() !== '') {
                        const li = document.createElement('li');
                        li.innerHTML = `<a href="#" data-category="${category}">${category}</a>`;
                        categoryList.appendChild(li);
                    }
                });
            } else {
                // 如果数据库中没有分类，使用默认分类
                const defaultCategories = ['臀部器械', '胸部器械', '背部器械', '肩部器械'];
                defaultCategories.forEach(category => {
                    const li = document.createElement('li');
                    li.innerHTML = `<a href="#" data-category="${category}">${category}</a>`;
                    categoryList.appendChild(li);
                });
            }
            
            // 为所有分类链接添加点击事件
            categoryList.querySelectorAll('a').forEach(link => {
                link.addEventListener('click', function(e) {
                    e.preventDefault();
                    const category = this.getAttribute('data-category') || this.textContent.trim();
                    loadEquipments(category, this);
                });
            });
            
            // 移除加载提示
            const loadingElement = categoryList.querySelector('.loading-categories');
            if (loadingElement) {
                loadingElement.remove();
            }
        }
        
        // 显示分类加载错误
        function showCategoryError() {
            const categoryList = document.getElementById('categoryList');
            if (categoryList) {
                const loadingElement = categoryList.querySelector('.loading-categories');
                if (loadingElement) {
                    loadingElement.textContent = '分类加载失败';
                    loadingElement.style.color = 'red';
                    
                    // 添加重试按钮
                    const retryBtn = document.createElement('button');
                    retryBtn.textContent = '重试';
                    retryBtn.style.marginLeft = '10px';
                    retryBtn.style.padding = '2px 8px';
                    retryBtn.style.background = '#00BE9A';
                    retryBtn.style.color = 'white';
                    retryBtn.style.border = 'none';
                    retryBtn.style.borderRadius = '3px';
                    retryBtn.style.cursor = 'pointer';
                    
                    retryBtn.addEventListener('click', function(e) {
                        e.stopPropagation();
                        loadingElement.textContent = '正在重新加载分类...';
                        loadingElement.style.color = '#999';
                        loadCategories();
                    });
                    
                    loadingElement.appendChild(retryBtn);
                }
            }
        }
        
        // 加载器材数据
        async function loadEquipments(category, clickedElement = null) {
            console.log(`正在加载器材数据 - 分类: ${category}`);
            
            // 更新活动标签
            updateActiveTab(category, clickedElement);
            
            // 显示加载状态
            showLoading();
            
            try {
                // 构建完整的请求URL
                const url = `${API_BASE_URL}/category/${encodeURIComponent(category)}`;
                console.log('完整请求URL:', url);
                
                // 发送请求
                const response = await fetch(url, {
                    method: 'GET',
                    mode: 'cors',
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json'
                    }
                });
                
                console.log('响应状态:', response.status, response.statusText);
                
                if (!response.ok) {
                    throw new Error(`HTTP错误! 状态码: ${response.status} ${response.statusText}`);
                }
                
                const result = await response.json();
                console.log('API响应:', result);
                
                if (result.code === 200) {
                    renderEquipmentList(result.data);
                } else {
                    throw new Error(result.message || 'API返回错误');
                }
                
            } catch (error) {
                console.error('加载器材数据失败:', error);
                showError(`加载失败: ${error.message}`);
            }
        }
        
        // 更新活动标签
        function updateActiveTab(category, clickedElement) {
            const allTabs = document.querySelectorAll('#categoryList a');
            
            // 移除所有active类
            allTabs.forEach(tab => tab.classList.remove('active'));
            
            // 如果有点击元素，激活它
            if (clickedElement) {
                clickedElement.classList.add('active');
                return;
            }
            
            // 否则根据数据属性或文本内容查找
            allTabs.forEach(tab => {
                const tabCategory = tab.getAttribute('data-category') || tab.textContent.trim();
                if (tabCategory === category) {
                    tab.classList.add('active');
                }
            });
        }
        
        // 显示加载状态
        function showLoading() {
            const listElement = document.getElementById('equipmentList');
            if (listElement) {
                listElement.innerHTML = `
                    <li style="grid-column:1/-1; text-align:center; padding:40px;">
                        <div style="width:40px; height:40px; margin:0 auto 15px; border:3px solid #f3f3f3; border-top:3px solid #00BE9A; border-radius:50%; animation:spin 1s linear infinite;"></div>
                        <p>正在加载器材数据...</p>
                        <style>
                            @keyframes spin {
                                0% { transform: rotate(0deg); }
                                100% { transform: rotate(360deg); }
                            }
                        </style>
                    </li>
                `;
            }
        }
        
        // 显示错误
        function showError(message) {
            const listElement = document.getElementById('equipmentList');
            if (listElement) {
                listElement.innerHTML = `
                    <li style="grid-column:1/-1; text-align:center; padding:40px; color:#d32f2f;">
                        <div style="font-size:40px; margin-bottom:15px;">⚠️</div>
                        <p style="margin-bottom:10px;">${message}</p>
                        <button onclick="retryLoad()" style="margin-top:10px; padding:8px 20px; background:#00BE9A; color:white; border:none; border-radius:4px; cursor:pointer;">
                            重试加载
                        </button>
                    </li>
                `;
            }
        }
        
        // 重试加载
        function retryLoad() {
            const activeTab = document.querySelector('#categoryList a.active');
            if (activeTab) {
                const category = activeTab.getAttribute('data-category') || activeTab.textContent.trim();
                loadEquipments(category);
            } else {
                loadEquipments('热门');
            }
        }
        
        // 渲染器材列表
        function renderEquipmentList(equipments) {
            const listElement = document.getElementById('equipmentList');
            if (!listElement) {
                console.error('找不到器材列表元素');
                return;
            }
            
            if (!equipments || equipments.length === 0) {
                listElement.innerHTML = `
                    <li style="grid-column:1/-1; text-align:center; padding:40px; color:#666;">
                        <p>暂无器材数据</p>
                        <p style="font-size:14px; margin-top:10px;">请检查分类名称是否正确或联系管理员</p>
                    </li>
                `;
                return;
            }
            
            listElement.innerHTML = '';
            
            equipments.forEach(equipment => {
                const li = document.createElement('li');
                
                // 处理图片URL
                let imageUrl = equipment.picture || equipment.imageUrl;
                if (!imageUrl || imageUrl.trim() === '') {
                    imageUrl = '../static/img/default-equipment.jpg';
                }
                
                // 处理价格
                let priceText = '价格待定';
                if (equipment.equipPrice) {
                    priceText = `¥${equipment.equipPrice.toFixed(2)}`;
                } else if (equipment.formattedPrice) {
                    priceText = equipment.formattedPrice;
                }
                
                // 处理状态
                let statusHtml = '';
                if (equipment.equipStatus && equipment.equipStatus !== '正常') {
                    const statusColor = equipment.equipStatus === '维修中' ? '#FF9800' : '#F44336';
                    statusHtml = `<p style="color:${statusColor}; font-size:12px; margin-top:5px;">${equipment.equipStatus}</p>`;
                }
                
                // 详情页链接
                const detailUrl = `equipment-detail.html?id=${equipment.equipId || equipment.id}`;
                
                li.innerHTML = `
                    <a href="${detailUrl}" target="_blank">
                        <div class="pic">
                            <img src="${imageUrl}" 
                                 alt="${equipment.equipName || '器材'}"
                                 onerror="this.src='../static/img/default-equipment.jpg'; this.onerror=null;">
                        </div>
                        <div class="txt">
                            <h4>${equipment.equipName || '未命名器材'}</h4>
                            <p>品牌：${equipment.equipBrand || '未知'}</p>
                            <p class="price">${priceText}</p>
                            ${statusHtml}
                        </div>
                    </a>
                `;
                
                listElement.appendChild(li);
            });
        }
        
        // 加载所有器材（查看全部）
        async function loadAllEquipments() {
            try {
                const response = await fetch(`${API_BASE_URL}/list`);
                if (!response.ok) {
                    throw new Error(`获取所有器材失败: ${response.status}`);
                }
                
                const equipments = await response.json();
                renderEquipmentList(equipments);
                
                // 更新活动状态
                document.querySelectorAll('#categoryList a').forEach(tab => {
                    tab.classList.remove('active');
                });
                
            } catch (error) {
                console.error('加载所有器材失败:', error);
                showError(`加载所有器材失败: ${error.message}`);
            }
        }
        
        // 暴露函数到全局
        window.loadEquipments = loadEquipments;
        window.retryLoad = retryLoad;
        window.loadAllEquipments = loadAllEquipments;