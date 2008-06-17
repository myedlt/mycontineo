update co_menus
   set co_menuref='search/keywords'
 where co_menuref='ShowKeywords.do';
 
 update co_menus
   set co_menuref='admin/users'
 where co_menuid=6;
 
 update co_menus
   set co_menuref='admin/groups'
 where co_menuid=7;
 
  update co_menus
   set co_menuref='admin/logs'
 where co_menuid=8;
 
 update co_menus
   set co_menuref='admin/searchEngine'
 where co_menuid=25;
 
 update co_menus
   set co_menuref='admin/backup'
 where co_menuid=27;
 
 update co_menus
   set co_menuref='document/browse'
 where co_menuid=5;

 update co_menus
   set co_menuref='admin/folders'
 where co_menuid=17;

 update co_menus
   set co_menuref='settings/password'
 where co_menuid=16;

 update co_menus
   set co_menuref='settings/personalData'
 where co_menuid=19;

 update co_menus
   set co_menuref='communication/messages'
 where co_menuid=13;

delete from co_menugroup
  where co_menuid=18;
delete from co_menus
  where co_menuid=18;
  
update co_menus
  set co_menuparent=2
  where co_menuid=17;
update co_menus
  set co_menusort=7
  where co_menuid=17;
  
delete from co_menugroup
  where co_menuid=3;
delete from co_menus
  where co_menuid=3;
  
delete from co_menugroup
  where co_menuid=28;
delete from co_menus
  where co_menuid=28;

delete from co_menugroup
  where co_menuid=9;
delete from co_menus
  where co_menuid=9;

delete from co_menugroup
  where co_menuid=10;
delete from co_menus
  where co_menuid=10;
  
delete from co_menugroup
  where co_menuid=11;
delete from co_menus
  where co_menuid=11;
  
delete from co_menugroup
  where co_menuid=12;
delete from co_menus
  where co_menuid=12;
  
delete from co_menugroup
  where co_menuid=14;
delete from co_menus
  where co_menuid=14;
  
delete from co_menugroup
  where co_menuid=15;
delete from co_menus
  where co_menuid=15;
  
delete from co_menugroup
  where co_menuid=21;
delete from co_menus
  where co_menuid=21;  
  
delete from co_menugroup
  where co_menuid=22;
delete from co_menus
  where co_menuid=22;
  
update co_menus
   set co_menuref='admin/accounts'
 where co_menuid=24;
 
 update co_menus
   set co_menuref='admin/smtp'
 where co_menuid=23;
 
drop table search_settings;

alter table co_menus 
add column co_menusize integer;

update co_menus
set co_menusize=0;

alter table co_account
 add column co_enabled integer;
 
 update co_account set co_enabled=1;