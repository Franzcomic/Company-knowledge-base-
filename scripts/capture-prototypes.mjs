// Reproducible UI prototypes with explicit fixture data; not a live RAG benchmark.
import fs from 'node:fs/promises';
import path from 'node:path';
import {createRequire} from 'node:module';
import {fileURLToPath, pathToFileURL} from 'node:url';
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..');
const runtime=process.env.RUNTIME_NODE_MODULES;
if(!runtime) throw new Error('Set RUNTIME_NODE_MODULES to the bundled package directory');
const require=createRequire(path.join(runtime,'__runtime__.cjs'));
const {chromium}=require('playwright');
const browser=await chromium.launch({channel:'chrome',headless:true});
const page=await browser.newPage({viewport:{width:1280,height:900},deviceScaleFactor:1.5});
const errors=[];
page.on('pageerror',e=>errors.push(e.message));
const out=path.join(root,'docs','diagrams');
await fs.mkdir(out,{recursive:true});
const figures=JSON.parse(await fs.readFile(path.join(out,'manifest.json'),'utf8'));
for(const fig of figures){
 await page.setViewportSize({width:fig.width,height:fig.height});
 await page.goto(pathToFileURL(path.join(out,fig.id+'.svg')).href);
 await page.screenshot({path:path.join(out,fig.id+'.png')});
}
if(process.argv.includes('--diagrams-only')) {
 await browser.close();
 console.log('Rendered 13 SVG diagrams.');
 process.exit(0);
}
await page.setViewportSize({width:1280,height:900});
const user={id:1,username:'admin',realName:'系统管理员',departmentId:3,roleId:1,roleCode:'SYS_ADMIN',roleName:'系统管理员'};
const departments=[{id:1,name:'财务部'},{id:2,name:'研发部'},{id:3,name:'全司'}];
const roles=[{id:1,code:'SYS_ADMIN',name:'系统管理员'},{id:2,code:'DEPT_ADMIN',name:'部门管理员'},{id:3,code:'EMPLOYEE',name:'员工'}];
const kb=[{id:1,name:'员工手册',departmentId:null,permissionLevel:'PUBLIC',description:'入职、休假与福利制度'},
 {id:2,name:'财务报销',departmentId:1,permissionLevel:'DEPT',description:'差旅报销与票据规范'},
 {id:3,name:'研发规范',departmentId:2,permissionLevel:'DEPT',description:'编码、评审与发布规范'}];
let conversations=[{id:1,title:'年假如何申请',createdAt:'2026-09-18 09:00'},{id:2,title:'出差报销材料',createdAt:'2026-09-17 15:30'}];
const sources=[{documentId:1,title:'01-01 员工手册.md',chunkId:'doc-1-0',similarity:.86}];
const messages=[{id:1,role:'USER',content:'年假如何申请？',createdAt:'2026-09-18 09:00'},
 {id:2,role:'ASSISTANT',content:'请通过 OA 提交休假申请，并按审批流程完成部门审批。具体申请条件与时间要求请查看下方员工手册的来源片段。',sources,answered:true,createdAt:'2026-09-18 09:00'}];
const documents=[{id:1,filename:'01-01 员工手册.md',fileType:'md',size:4517,status:'READY',chunkCount:11,permissionLevel:'PUBLIC',createdAt:'2026-09-18 09:00'},
 {id:2,filename:'员工福利制度.pdf',fileType:'pdf',size:18432,status:'PARSING',chunkCount:0,permissionLevel:'PUBLIC',createdAt:'2026-09-18 09:05'},
 {id:3,filename:'保密管理制度.docx',fileType:'docx',size:20548,status:'FAILED',chunkCount:0,permissionLevel:'CONFIDENTIAL',createdAt:'2026-09-18 09:06'}];
let messageCalls=0;
const unknown=[];
await page.route(url=>url.pathname.startsWith('/api/'),async route=>{
 const req=route.request(),url=new URL(req.url()),p=url.pathname;
 let data;
 if(p==='/api/auth/login') data={token:'prototype-only',user};
 else if(p==='/api/auth/me') data=user;
 else if(p==='/api/conversations' && req.method()==='POST') { const c={id:3,title:null,createdAt:'2026-09-18 10:00'}; conversations=[c,...conversations]; data=c; }
 else if(p==='/api/conversations') data=conversations;
 else if(/^\/api\/conversations\/\d+\/messages$/.test(p)) data=p.includes('/3/')?[]:messages;
 else if(p==='/api/messages') { messageCalls++; await new Promise(r=>setTimeout(r,350)); data={messageId:4,content:messages[1].content,sources,answered:true}; }
 else if(p==='/api/kb') data=kb;
 else if(p==='/api/departments') data=departments;
 else if(p==='/api/roles') data=roles;
 else if(p==='/api/users') data=[{...user,status:1},{id:2,username:'zhangsan',realName:'张三',departmentId:1,roleId:2,roleCode:'DEPT_ADMIN',roleName:'部门管理员',status:1},{id:3,username:'lisi',realName:'李四',departmentId:1,roleId:3,roleCode:'EMPLOYEE',roleName:'员工',status:1}];
 else if(/\/kb\/\d+\/documents$/.test(p)) data=documents;
 else if(/\/documents\/\d+\/chunks$/.test(p)) data=[{chunkIndex:0,content:'示例片段：员工请假需通过 OA 提交申请，并由部门负责人审批。',similarity:null}];
 else if(/\/documents\/\d+$/.test(p)) data=documents[1];
 else if(p==='/api/stats/overview') data={total:126,answered:108,notAnswered:18,hitRate:108/126,avgResponseMs:6200,satisfaction:.91,trend:[12,18,15,22,17,24,18].map((n,i)=>({date:`09-${12+i}`,total:n}))};
 else if(p==='/api/stats/hot-questions') data=[{question:'年假如何申请',count:28},{question:'差旅报销流程',count:21},{question:'VPN连接方法',count:13}];
 else if(p==='/api/stats/hot-kb') data=[{kbName:'员工手册',count:46},{kbName:'财务报销',count:35},{kbName:'IT 运维',count:27}];
 else if(p==='/api/stats/departments') data=[{deptName:'财务部',count:42},{deptName:'研发部',count:56},{deptName:'行政部',count:28}];
 else if(p==='/api/stats/unsolved') data=['外部合作伙伴的合同模板在哪里？'];
 else { unknown.push(p); data=null; }
 await route.fulfill({status:200,contentType:'application/json',body:JSON.stringify({code:200,msg:'success',data})});
});
async function snap(id){
 await page.waitForTimeout(450);
 await page.evaluate(()=>{
  document.querySelectorAll('.el-message').forEach(el=>el.remove());
  document.getElementById('prototype-label')?.remove();
  const label=document.createElement('div'); label.id='prototype-label'; label.textContent='界面原型 · 示例数据';
  label.style.cssText='position:fixed;bottom:8px;right:12px;z-index:9999;background:#fff;border:1px solid #cbd5e1;padding:6px 12px;color:#475569;font:13px Microsoft YaHei;'; document.body.append(label);
 });
 await page.screenshot({path:path.join(out,id+'.png')});
}
const base=process.env.PROTOTYPE_URL||'http://127.0.0.1:5181';
await page.goto(base+'/login'); await page.getByPlaceholder('请输入用户名').fill('admin'); await snap('prototype-login');
await page.getByPlaceholder('请输入密码',{exact:true}).fill('fixture-password');
await page.getByRole('button',{name:'登 录'}).click();
await page.waitForURL('**/chat'); await page.getByText('请通过 OA 提交休假申请', {exact:false}).waitFor();
await snap('prototype-chat');
await page.getByText('出差报销材料',{exact:true}).click(); await snap('prototype-history');
await page.getByRole('button',{name:'新建会话'}).click();
await page.getByRole('button',{name:'年假如何申请？',exact:true}).waitFor(); await snap('prototype-faq');
await page.getByRole('button',{name:'年假如何申请？',exact:true}).click();
await page.getByRole('button',{name:'发送',exact:true}).click();
await page.getByText('请通过 OA 提交休假申请',{exact:false}).waitFor();
if(messageCalls!==1) throw new Error('Send must issue exactly one message request');
await page.locator('.source-card').first().click();
await page.getByText('示例片段：员工请假需通过 OA 提交申请，并由部门负责人审批。',{exact:true}).waitFor();
await page.goto(base+'/kb/1/documents'); await page.getByText('01-01 员工手册.md',{exact:true}).waitFor(); await snap('prototype-kb');
await page.goto(base+'/admin/users'); await page.getByText('张三',{exact:true}).waitFor(); await snap('prototype-users');
await page.goto(base+'/admin/stats'); await page.getByText('126',{exact:true}).waitFor(); await page.waitForTimeout(1200); await snap('prototype-stats');
await fs.mkdir(path.join(root,'.work'),{recursive:true});
await fs.writeFile(path.join(root,'.work','frontend-check.json'),JSON.stringify({kind:'fixture-backed browser smoke test',errors,unknown,messageCalls,pages:7},null,2));
await browser.close();
if(errors.length||unknown.length) throw new Error(JSON.stringify({errors,unknown}));
console.log('Rendered 13 SVG diagrams; captured 7 prototypes; login, navigation, FAQ, send and source preview passed.');
