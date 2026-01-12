# AI 聊天语音输入功能说明

## 功能概述
为 AI 聊天组件添加了语音输入功能，使用浏览器原生的 Web Speech API 实现语音转文字功能。

## 实现的功能

### 1. 语音识别
- ✅ 使用浏览器的 Web Speech API (SpeechRecognition)
- ✅ 支持中文语音识别 (zh-CN)
- ✅ **连续识别模式** - 不会自动停止，持续监听
- ✅ 实时显示识别结果
- ✅ 自动将语音转换为文字并填入输入框
- ✅ 只有用户主动点击"停止"或遇到严重错误才会停止

### 2. 用户交互
- ✅ 点击麦克风按钮开始/停止语音输入
- ✅ 监听状态下按钮显示动画效果
- ✅ 输入框 placeholder 提示当前状态
- ✅ 浏览器兼容性检测和错误提示

### 3. UI 设计
- ✅ 美观的麦克风图标按钮
- ✅ 监听状态下的脉冲动画效果
- ✅ 渐变色背景增强视觉效果
- ✅ 响应式布局适配

## 技术实现

### TypeScript 类型定义
```typescript
// 添加了 SpeechRecognition 接口定义
interface SpeechRecognition extends EventTarget {
  lang: string;
  continuous: boolean;
  interimResults: boolean;
  start(): void;
  stop(): void;
  abort(): void;
  onresult: ((this: SpeechRecognition, ev: any) => any) | null;
  onerror: ((this: SpeechRecognition, ev: any) => any) | null;
  onend: ((this: SpeechRecognition, ev: any) => any) | null;
}
```

### 核心功能代码
```typescript
// 语音识别状态
const [isListening, setIsListening] = useState(false);
const recognitionRef = useRef<SpeechRecognition | null>(null);

// 初始化语音识别
useEffect(() => {
  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
  
  if (SpeechRecognition) {
    const recognition = new SpeechRecognition();
    recognition.lang = 'zh-CN'; // 中文
    recognition.continuous = true; // 🔥 连续识别，不会自动停止
    recognition.interimResults = true; // 显示中间结果
    
    // 识别结果处理
    recognition.onresult = (event) => {
      const transcript = Array.from(event.results)
        .map((result) => result[0])
        .map((result) => result.transcript)
        .join('');
      setInputValue(transcript);
    };
    
    // 只有在用户主动停止或严重错误时才会触发
    recognition.onend = () => {
      setIsListening(false);
    };
    
    // 错误处理 - 只对严重错误停止识别
    recognition.onerror = (event) => {
      if (event.error === 'not-allowed' || event.error === 'network') {
        setIsListening(false);
        // 显示错误提示
      }
      // 对于 'no-speech' 等轻微错误，继续监听
    };
    
    recognitionRef.current = recognition;
  }
}, []);

// 切换语音识别
const toggleVoiceInput = () => {
  if (!recognitionRef.current) {
    alert('抱歉，您的浏览器不支持语音识别功能');
    return;
  }
  
  if (isListening) {
    recognitionRef.current.stop(); // 用户主动停止
  } else {
    recognitionRef.current.start(); // 开始连续监听
  }
  setIsListening(!isListening);
};
```

### CSS 样式
- 语音按钮使用渐变色背景
- 监听状态下添加脉冲动画
- 按钮悬停效果
- 响应式布局

## 浏览器兼容性

### 支持的浏览器
- ✅ Chrome (推荐)
- ✅ Edge
- ✅ Safari (部分支持)
- ✅ Opera

### 不支持的浏览器
- ❌ Firefox (不支持 Web Speech API)
- ❌ IE (不支持)

## 使用说明

### 用户操作步骤
1. 点击输入框旁边的 🎤 麦克风按钮
2. 允许浏览器访问麦克风（首次使用需要授权）
3. 开始说话，系统会**持续监听**并实时识别转换为文字
4. 可以**连续说话，不会中断**，识别结果会不断更新
5. 说完后点击"🎤 停止"按钮手动停止录音
6. 编辑识别的文字（如需要）
7. 点击"发送"按钮发送消息

### 注意事项
- 首次使用需要授权麦克风权限
- **语音识别会持续监听，不会自动停止**
- 可以连续说多句话，系统会实时更新识别结果
- 说完后**必须手动点击"停止"按钮**结束录音
- 请在安静的环境中使用以获得最佳识别效果
- 说话要清晰，语速适中
- 如果识别结果不准确，可以手动编辑

## 错误处理

### 权限被拒绝
```
提示："请允许麦克风权限以使用语音输入功能"
```

### 未检测到语音
```
不会停止识别，会继续监听（这是连续模式的优势）
```

### 浏览器不支持
```
提示："抱歉，您的浏览器不支持语音识别功能。请使用 Chrome、Edge 等现代浏览器。"
```

## 未来改进方向
- [ ] 支持更多语言（英语、日语等）
- [ ] 添加语音输入历史记录
- [ ] 优化识别准确率
- [ ] 添加语音命令（如"发送"、"清空"等）
- [ ] 支持连续对话模式

## 测试建议
1. 测试中文普通话识别
2. 测试不同方言（如粤语、四川话）
3. 测试在噪音环境下的表现
4. 测试长句子的识别准确率
5. 测试麦克风权限的各种场景

## 开发者说明
如需修改语音识别配置，请在 `AiChat.tsx` 中的 `useEffect` hook 中修改：
- `recognition.lang`: 修改识别语言
- `recognition.continuous`: **true = 连续识别（当前设置），false = 识别一次就停止**
- `recognition.interimResults`: 是否显示中间结果

### 连续识别模式的优势
✅ 用户可以连续说话，不会被打断  
✅ 适合长文本输入  
✅ 更自然的语音交互体验  
✅ 用户完全控制何时停止录音
