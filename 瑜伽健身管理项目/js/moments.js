// 朋友圈功能逻辑
Vue.component('moments', {
    template: '#moments-template',
    data() {
        return {
            showMoments: false,
            showPublishDialog: false,
            loadingMoments: false,
            publishing: false,
            uploadUrl: 'http://127.0.0.1:8080/api/upload/image',
            uploadHeaders: {
                'Authorization': `Bearer ${Cookies.get('token')}`
            },
            
            // 发布表单
            publishForm: {
                content: '',
                images: [],
                visibility: 'public'
            },
            
            publishRules: {
                content: [
                    { required: true, message: '请输入分享内容', trigger: 'blur' },
                    { min: 1, max: 500, message: '长度在 1 到 500 个字符', trigger: 'blur' }
                ]
            },
            
            // 朋友圈数据
            momentsList: [],
            pageNum: 1,
            pageSize: 10,
            totalCount: 0,
            
            // 图片预览
            previewVisible: false,
            previewImages: [],
            previewIndex: 0
        };
    },
    
    computed: {
        userRole() {
            return this.$parent.userRole;
        },
        userId() {
            return this.$parent.userId;
        }
    },
    
    methods: {
        // 重置发布表单
        resetPublishForm() {
            this.publishForm = {
                content: '',
                images: [],
                visibility: 'public'
            };
            if (this.$refs.publishFormRef) {
                this.$refs.publishFormRef.clearValidate();
            }
        },
        
        // 发布动态
        async publishMoment() {
            this.$refs.publishFormRef.validate(async (valid) => {
                if (!valid) return;
                
                // 普通用户权限检查
                if (this.userRole === 4) {
                    this.$message.error('你还不是黄金用户，请升级为黄金用户');
                    this.showPublishDialog = false;
                    return;
                }
                
                this.publishing = true;
                try {
                    const imageUrls = this.publishForm.images.map(img => ({
                        url: img.response?.data?.url || img.url,
                        name: img.name
                    }));
                    
                    const response = await axios.post('http://127.0.0.1:8080/api/moments/publish', {
                        content: this.publishForm.content,
                        images: imageUrls,
                        visibility: this.publishForm.visibility
                    });
                    
                    if (response.data.code === 200) {
                        this.$message.success('发布成功！');
                        this.showPublishDialog = false;
                        this.resetPublishForm();
                        this.loadMoments(); // 重新加载朋友圈
                    } else {
                        this.$message.error(response.data.message);
                    }
                } catch (error) {
                    console.error('发布失败:', error);
                    this.$message.error('发布失败，请稍后重试');
                } finally {
                    this.publishing = false;
                }
            });
        },
        
        // 加载朋友圈
        async loadMoments() {
            this.loadingMoments = true;
            try {
                const response = await axios.get('http://127.0.0.1:8080/api/moments/list', {
                    params: {
                        pageNum: this.pageNum,
                        pageSize: this.pageSize
                    }
                });
                
                if (response.data.code === 200) {
                    this.momentsList = response.data.data.records.map(moment => ({
                        ...moment,
                        showCommentInput: false,
                        commentText: '',
                        isLiked: moment.likes?.some(like => like.userId === this.userId) || false
                    }));
                    this.totalCount = response.data.data.total;
                }
            } catch (error) {
                console.error('加载朋友圈失败:', error);
                this.$message.error('加载动态失败');
            } finally {
                this.loadingMoments = false;
            }
        },
        
        // 点赞/取消点赞
        async toggleLike(moment) {
            try {
                const response = await axios.post(
                    `http://127.0.0.1:8080/api/moments/like/${moment.id}`
                );
                
                if (response.data.code === 200) {
                    moment.isLiked = !moment.isLiked;
                    moment.likeCount = response.data.data.likeCount;
                    moment.likes = response.data.data.likes;
                }
            } catch (error) {
                console.error('操作失败:', error);
            }
        },
        
        // 提交评论
        async submitComment(moment) {
            if (!moment.commentText.trim()) return;
            
            try {
                const response = await axios.post(
                    `http://127.0.0.1:8080/api/moments/comment/${moment.id}`,
                    { content: moment.commentText }
                );
                
                if (response.data.code === 200) {
                    moment.commentText = '';
                    moment.showCommentInput = false;
                    moment.comments = response.data.data.comments;
                    moment.commentCount = response.data.data.commentCount;
                }
            } catch (error) {
                console.error('评论失败:', error);
                this.$message.error('评论失败');
            }
        },
        
        // 显示评论输入框
        showCommentInput(moment) {
            moment.showCommentInput = true;
            this.$nextTick(() => {
                const input = document.querySelector(`#moment-${moment.id} .comment-input input`);
                if (input) input.focus();
            });
        },
        
        // 分享选项
        showShareOptions(moment) {
            this.$message.success('分享功能开发中...');
            // 这里可以添加微信分享、复制链接等功能
        },
        
        // 图片上传相关
        handleUploadSuccess(response, file, fileList) {
            if (response.code === 200) {
                this.publishForm.images = fileList;
                this.$message.success('上传成功');
            } else {
                this.$message.error(response.message || '上传失败');
            }
        },
        
        handleUploadError(err, file, fileList) {
            this.$message.error('上传失败，请检查网络或文件大小');
        },
        
        handleRemove(file, fileList) {
            this.publishForm.images = fileList;
        },
        
        handleExceed(files, fileList) {
            this.$message.warning('最多只能上传9张图片');
        },
        
        // 图片预览
        previewImage(images, index) {
            this.previewImages = images.map(img => img.url);
            this.previewIndex = index;
            this.previewVisible = true;
        },
        
        // 分页
        handleSizeChange(val) {
            this.pageSize = val;
            this.loadMoments();
        },
        
        handleCurrentChange(val) {
            this.pageNum = val;
            this.loadMoments();
        },
        
        // 时间格式化
        formatTime(timestamp) {
            const date = new Date(timestamp);
            const now = new Date();
            const diff = now - date;
            
            if (diff < 60000) return '刚刚';
            if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`;
            if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`;
            if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`;
            
            return date.toLocaleDateString();
        }
    },
    
    watch: {
        showMoments(val) {
            if (val) {
                this.loadMoments();
            }
        }
    }
});

// 在主Vue实例中混入朋友圈功能
const originalCreate = Vue.options.created;
Vue.options.created = function() {
    if (originalCreate) originalCreate.call(this);
    
    // 监听朋友圈菜单点击
    this.$on('show-moments', () => {
        this.showMoments = true;
    });
};