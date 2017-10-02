class eventPlan{
  constructor(){
		var events = [];   
  }
   add(name,year,month,day,time){
     events.push({'name':name,'year':year,'month':month,'time':time});
   }
  printEvents(){
    console.log(events);
  }
}

var e = eventPlan; 
var name, year, month, day, time;

while(1===1){
  console.log('Add event(ADD) or quit(Q)');
  var input = readline();
  if (input === 'ADD'){
    console.log('What is the event?');
    name = readline();
    console.log('What is the year?');
    year = readline();
    console.log('What is the month?');
    month = readline();
    console.log('What is the day?');
    day = readline();
    console.log('What is the time?');
    time = readline();
    e.add(name,year,month,day,time);
    
  }else if (input==='Q'){
    break;
  }
}

e.print();








