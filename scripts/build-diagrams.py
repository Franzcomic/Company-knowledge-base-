"""Generate editable SVG architecture/UML diagrams. No network or model required."""
from pathlib import Path
from html import escape
import json

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'docs' / 'diagrams'
OUT.mkdir(parents=True, exist_ok=True)
manifest = []

class Canvas:
    def __init__(self, name, title, w=1100, h=700):
        self.name,self.title,self.w,self.h=name,title,w,h
        self.parts=[f'<svg xmlns="http://www.w3.org/2000/svg" width="{w}" height="{h}" viewBox="0 0 {w} {h}"><defs><marker id="arrow" markerWidth="10" markerHeight="8" refX="9" refY="4" orient="auto"><path d="M0,0 L10,4 L0,8" fill="none" stroke="#475569"/></marker></defs><rect width="100%" height="100%" fill="white"/>']
        self.text(w/2,38,title,26,bold=True)
    def text(self,x,y,text,size=20,bold=False,anchor='middle',fill='#172b4d'):
        for i,line in enumerate(text.split('\n')):
            self.parts.append(f'<text x="{x}" y="{y+i*(size+7)}" text-anchor="{anchor}" fill="{fill}" font-size="{size}" font-weight="{600 if bold else 400}" font-family="Microsoft YaHei, Noto Sans CJK SC, sans-serif">{escape(line)}</text>')
    def box(self,x,y,w,h,text,fill='#eff5fb',size=20):
        self.parts.append(f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="8" fill="{fill}" stroke="#7491ad" stroke-width="1.5"/>')
        self.text(x+w/2,y+h/2-((len(text.split(chr(10)))-1)*(size+7))/2+size*.35,text,size)
    def edge(self,points,label='',dashed=False,labelpos=None):
        p=' '.join(f'{x},{y}' for x,y in points)
        self.parts.append(f'<polyline points="{p}" fill="none" stroke="#475569" stroke-width="1.6" marker-end="url(#arrow)" {"stroke-dasharray=\"6 5\"" if dashed else ""}/>')
        if label:
            x,y=labelpos or ((points[0][0]+points[-1][0])/2,(points[0][1]+points[-1][1])/2-8)
            self.text(x,y,label,16)
    def ellipse(self,x,y,w,h,text):
        self.parts.append(f'<ellipse cx="{x+w/2}" cy="{y+h/2}" rx="{w/2}" ry="{h/2}" fill="#f0f6fb" stroke="#7491ad" stroke-width="1.5"/>')
        self.text(x+w/2,y+h/2+7,text,20)
    def diamond(self,x,y,w,h,text):
        self.parts.append(f'<polygon points="{x+w/2},{y} {x+w},{y+h/2} {x+w/2},{y+h} {x},{y+h/2}" fill="#fff5e8" stroke="#b38a55" stroke-width="1.5"/>')
        self.text(x+w/2,y+h/2+7,text,18)
    def actor(self,x,y,name):
        self.parts.append(f'<g stroke="#334155" stroke-width="2" fill="none"><circle cx="{x}" cy="{y}" r="15"/><path d="M{x},{y+15} v45 m-30,-26 h60 m-30,26 l-25,35 m25,-35 l25,35"/></g>')
        self.text(x,y+120,name,19)
    def save(self):
        (OUT/(self.name+'.svg')).write_text(''.join(self.parts)+'</svg>',encoding='utf-8')
        manifest.append({'id':self.name,'title':self.title,'width':self.w,'height':self.h})

c=Canvas('architecture','系统总体架构',1100,700)
c.box(365,80,370,65,'浏览器 · Vue 3 + Element Plus')
c.box(255,205,590,70,'Spring Boot 4 · JWT 认证与业务服务')
c.edge([(550,145),(550,205)],'REST / JSON')
c.box(40,345,300,80,'MySQL 8.4\n用户 · 知识库 · 会话 · 评价')
c.box(400,345,300,80,'Spring AI 2.0\n解析 · 分块 · 检索 · 提示词')
c.box(760,345,300,80,'文件存储\nPDF / DOCX / TXT / MD')
for x in [190,550,910]: c.edge([(550,275),(550,305),(x,305),(x,345)])
c.box(210,530,300,80,'Milvus 向量数据库\n文本块 · 向量 · 权限元数据')
c.box(600,530,300,80,'本地模型服务\nEmbedding + Chat')
c.edge([(490,425),(490,475),(360,475),(360,530)])
c.edge([(610,425),(610,475),(750,475),(750,530)])
c.text(550,660,'原始文档和检索片段均受部门与角色权限约束',18)
c.save()

for name,title,labels,actors in [
 ('usecase-main','系统主用例图',['登录系统','智能问答','查看来源与历史','评价回答','维护知识库','上传与处理文档','管理用户权限','查看问答统计'],['员工','部门管理员','系统管理员']),
 ('usecase-employee','员工端用例图',['登录系统','提出知识问题','查看引用片段','查看历史会话','继续追问','评价回答'],['员工']),
 ('usecase-admin','管理端用例图',['维护知识库','上传与删除文档','查看入库状态','调整文档权限','调整用户角色','查看统计报表'],['部门管理员','系统管理员'])]:
 c=Canvas(name,title,1100,700)
 c.parts.append('<rect x="255" y="80" width="605" height="570" fill="none" stroke="#94a3b8" stroke-dasharray="6 4"/>')
 c.text(555,111,'企业内部知识库系统',18)
 for i,label in enumerate(labels):
  col=i//4 if len(labels)>6 else i//3
  row=i%4 if len(labels)>6 else i%3
  c.ellipse(290+col*280,145+row*120,240,66,label)
 if name=='usecase-main':
  c.actor(110,290,'员工'); c.actor(990,210,'部门管理员'); c.actor(990,470,'系统管理员')
  for y in [178,298,418,538]: c.edge([(145,330),(290,y)])
  for y in [178,298]: c.edge([(958,250),(810,y)])
  for y in [418,538]: c.edge([(958,510),(810,y)])
  c.text(990,642,'继承员工能力',15)
 elif name=='usecase-employee':
  c.actor(105,300,'员工')
  for y in [178,298,418]: c.edge([(140,340),(290,y)])
  for y in [178,298,418]: c.edge([(110,420),(110,595),(840,595),(840,y),(810,y)])
 else:
  c.actor(100,300,'部门管理员'); c.actor(1000,300,'系统管理员')
  for y in [178,298,418]: c.edge([(135,340),(290,y)])
  for y in [178,298,418]: c.edge([(965,340),(810,y)])
  c.text(550,575,'部门管理员：仅本部门资源',20)
  c.text(550,611,'系统管理员：全局资源与账号管理',20)
 c.save()

def flow(name,title,steps,note):
 c=Canvas(name,title,1000,900)
 for i,step in enumerate(steps):
  y=85+i*96
  if step.endswith('？'): c.diamond(240,y,520,63,step)
  else: c.box(280,y,440,63,step,size=20)
  if i: c.edge([(500,y-33),(500,y)])
 c.text(500,870,note,17)
 return c
c=flow('flow-ingest','文档入库活动图',['管理员提交文档','检查部门权限 · 文件类型 · 大小','保存原文件与 PARSING 记录','异步解析与清洗文本','按 500 字分块 · 重叠 100 字','Embedding → Milvus 写入','成功 READY / 失败 FAILED','轮询状态 · 失败可重新处理'],'失败重试最多 3 次；删除与重处理须校验管理权限')
c.save()
c=flow('flow-qa','智能问答活动图',['接收问题与会话 ID','JWT 验证 · 核对会话归属','生成权限过滤条件','问题向量化 · Milvus Top-K','阈值与权限复核后有依据？','是：取前 3 块构建上下文','调用本地 LLM · 返回来源','保存问答 · 前端显示与评价'],'无可靠片段或空回答：拒答；模型服务异常：返回错误提示')
c.box(25,480,205,100,'没有可靠依据\n返回兜底提示',fill='#fff5e8',size=19)
c.edge([(240,500),(230,500)],'否',labelpos=(247,476))
c.save()

c=Canvas('modules','系统功能模块图',1100,560)
c.box(345,85,410,70,'企业内部知识库客服系统')
for i,(title,body) in enumerate([('用户与权限','登录认证\n部门与角色\n资源授权'),('知识库管理','知识库维护\n文件上传\n状态与分块'),('智能问答','权限检索\n答案与引用\n多轮上下文'),('会话与评价','个人历史\n继续追问\n有用与无用'),('统计分析','问答数量\n拒答与命中\n满意率趋势')]):
 x=20+i*216
 c.box(x,250,196,195,title+'\n\n'+body,size=19)
 c.edge([(550,155),(550,205),(x+98,205),(x+98,250)])
c.save()

def classes(name,title,items,links):
 c=Canvas(name,title,1100,650)
 coords=[]
 for i,(head,body) in enumerate(items):
  x=30+(i%3)*365; y=100+(i//3)*270
  c.box(x,y,310,175,'',size=18)
  c.text(x+155,y+33,head,18,True)
  c.parts.append(f'<path d="M{x},{y+50} h310" stroke="#7491ad"/>')
  c.text(x+15,y+83,body,17,anchor='start')
  coords.append((x,y))
 for j,(a,b) in enumerate(links):
  ax,ay=coords[a]; bx,by=coords[b]
  if ay==by: c.edge([(ax+310,ay+100),(bx,by+100)])
  else:
   offset=(j-2)*18
   c.edge([(ax+110+offset,ay+175),(ax+110+offset,by-70+offset),(bx+110+offset,by-70+offset),(bx+110+offset,by)])
 c.text(550,626,'箭头表示调用或依赖；名称对应 backend/src/main/java/com/corpedia',16)
 c.save()
classes('class-ingest','文档处理核心类图',[
 ('DocumentService','+ upload(kbId, file, userId)\n+ reprocess(id)\n+ updatePermission(id, req)'),
 ('DocumentPipelineService','+ ingest(documentId)\n+ runPipeline(documentId)\n- parseFile(doc)'),
 ('Chunker','+ split(id, text, metadata)\n- rag: RagProperties'),
 ('ResourceAccessService','+ requireRead(document)\n+ requireManage(deptId)\n+ canUseSource(documentId)'),
 ('TextCleaner','+ clean(raw)\n规范空白与重复段落'),
 ('VectorStore','+ add(chunks)\n+ similaritySearch(request)\n内部调用 EmbeddingModel')],[(0,1),(1,2),(0,3),(1,4),(1,5)])
classes('class-chat','问答核心类图',[
 ('MessageController','+ send(request)\n校验输入并获取当前用户'),
 ('MessageService','+ ask(userId, convId, text)\n校验会话并保存问题与回答'),
 ('RagChatService','+ chat(userId, convId, text)\n阈值过滤与上下文构建'),
 ('ConversationService','+ requireConversation(uid,id)\n+ messages(uid,id)\n+ list(uid)'),
 ('RagRetrieveService','+ retrieve(query,k,filter)\n+ rerank(hits,n)\n相似度排序与截取'),
 ('ChatClient','+ prompt().system().user()\n+ call().content()\n调用本地语言模型')],[(0,1),(1,2),(1,3),(2,4),(2,5)])

def sequence(name,title,participants,messages,note):
 c=Canvas(name,title,1160,780)
 xs=[80+i*200 for i in range(len(participants))]
 for x,label in zip(xs,participants):
  c.box(x-70,80,140,72,label,size=16)
  c.parts.append(f'<path d="M{x},152 V706" stroke="#94a3b8" stroke-dasharray="5 5"/>')
 for i,(a,b,msg,dashed) in enumerate(messages):
  y=205+i*61
  c.edge([(xs[a],y),(xs[b],y)],msg,dashed,labelpos=((xs[a]+xs[b])/2,y-12))
 c.text(580,750,note,17)
 c.save()
sequence('sequence-ingest','文档入库时序图',['管理员\n浏览器','Document\nService','MySQL','Pipeline\n异步任务','Embedding\n模型','Milvus'],[
 (0,1,'1  上传文件',False),(1,2,'2  权限检查后保存 PARSING',False),(1,3,'3  触发异步处理',False),
 (1,0,'4  返回文档 ID 与状态',True),(3,5,'5  解析清洗分块；删除旧向量',False),(3,4,'6  VectorStore 请求向量化',False),
 (3,5,'7  写入新分块与向量',False),(3,2,'8  更新 READY 或 FAILED',False)],'浏览器通过 GET /api/documents/{id} 轮询最终状态')
sequence('sequence-chat','检索增强问答时序图',['员工\n浏览器','Message /\nRagChat 服务','权限与\nMySQL','RagRetrieve\nService','Milvus','本地 LLM'],[
 (0,1,'1  POST /api/messages',False),(1,2,'2  校验本人会话并保存问题',False),(1,3,'3  按用户权限检索',False),
 (3,4,'4  向量化与 Top-K 过滤检索',False),(4,3,'5  返回候选片段',True),(1,2,'6  复核片段当前权限',False),
 (1,5,'7  可靠片段与历史上下文',False),(5,1,'8  返回答案；保存并附来源',True)],'没有可靠片段时直接拒答，不调用 LLM；历史会话按用户隔离')
c=flow('access','资源访问控制流程',['读取 Bearer JWT 并验签','重新读取当前账号与角色','账号有效且启用？','读取知识库与文档所属部门','依据读写操作计算可访问范围','符合部门与角色权限？','是：允许访问相应资源','RAG 生成前再次复核来源权限'],'读：部门与等级交集；写：系统管理员或本部门管理员')
c.box(20,276,180,60,'返回 401',fill='#fff5e8'); c.edge([(240,308),(200,308)],'否',labelpos=(220,295))
c.box(20,564,180,60,'返回 403',fill='#fff5e8'); c.edge([(240,596),(200,596)],'否',labelpos=(220,580))
c.save()

c=Canvas('er','业务数据库实体关系图',1100,940)
entities=[('department','id PK\nname\nparent_id'),('user','id PK\ndepartment_id FK\nrole_id FK'),('role','id PK\ncode\nname'),
 ('knowledge_base','id PK\ndepartment_id FK\ncreated_by FK'),('conversation','id PK\nuser_id FK\ntitle'),('feedback','id PK\nmessage_id FK\nuser_id FK'),
 ('document','id PK\nkb_id FK\ndepartment_id FK'),('message','id PK\nconversation_id FK\ncontent / sources'),('Milvus chunk','doc_id PK\nmetadata.document_id\ncontent / embedding')]
for i,(name,fields) in enumerate(entities):
 x=35+(i%3)*365; y=100+(i//3)*250
 c.box(x,y,285,172,'')
 c.text(x+142,y+30,name,21,True); c.text(x+18,y+65,fields,18,anchor='start')
c.edge([(320,160),(400,160)],'1 : N')
c.edge([(765,220),(685,220)],'1 : N')
c.edge([(170,272),(170,350)],'1 : N',labelpos=(210,316))
c.edge([(540,272),(540,350)],'1 : N',labelpos=(580,316))
c.edge([(170,522),(170,600)],'1 : N',labelpos=(210,567))
c.edge([(540,522),(540,600)],'1 : N',labelpos=(580,567))
c.edge([(685,670),(730,670),(730,440),(765,440)],'1 : N',labelpos=(715,563))
c.edge([(320,758),(355,758),(355,805),(908,805),(908,772)],'1 : N',labelpos=(610,832))
c.text(550,884,'FK 表示逻辑关联，当前 DDL 由服务层维护关系；图中省略次要字段',17)
c.text(550,914,'permission 与 qa_statistics 为预留表；统计当前直接聚合消息和评价',17)
c.save()
(OUT/'manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2),encoding='utf-8')
print(f'Generated {len(manifest)} SVG diagrams in {OUT}')
