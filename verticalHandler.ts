class VerticalHandler {
    public static generate(s:string){
        let ret:string[] = [];
        s = s.replace("=","");
        s = s.replace("＝", "");        
        s = s.replace("*","×");
        s = s.replace("/","÷");
        let arr:string[] = s.split(/[+\\-×÷]{1}/g);
        if(arr.length < 2){
            return null;
        }
        let curIndex:number = 0;
        for(let i=0;i<arr.length;i++){
            ret.push(arr[i]);
            curIndex += arr[i].length;
            if(i!=arr.length-1){
                ret.push(s[curIndex]);
                curIndex ++;
            }
        }

        let ret2:string[] = [];
        let ret3:string[] = [];
        let fans:string = ""; 
        ret2.push(ret[0]);
        let ty = 0;
        for(let i=1;i<ret.length; i+=2){
            let tmp:string[] = [
                ret2[ret2.length-1],
                ret[i],
                ret[i+1]
            ]
            let tmp2:string[];
            
            let op:string = tmp[1];
            let tmpArr = [tmp[0], tmp[2]];
            if(op == "÷"){
                ty = 4;
                tmp = this.generateDivide2(tmpArr, tmp);
                tmp2 = this.eqToTxtDivide(tmp);
            }else if(op == "×"){
                ty = 3;
                tmp = this.generateMult(tmpArr, tmp);
                tmp2 = this.eqToTxtMult(tmp);
            }else{         
                if(op == "+"){
                    ty = 1;
                }else{
                    ty = 2;
                }
                tmp = this.generateAddSub(tmpArr, tmp, arr);
                tmp2 = this.eqToTxtAddSub(tmp);
            }
            if(ret3.length == 0){
                ret3 = ret3.concat(tmp2);
            }else{ 
                ret3.pop();
                ret3 = ret3.concat(tmp2);
            }
            ret2 = ret2.concat(tmp)        
        }        
        let ret4 = null
        // ret4 = ["10?", "___", "6/6?5", "?口口", "___", "5"]
        // ret4 = ["605", "× ?6", "____", "3?30", "60?口", "____", "9??0"]
        console.log("ret3 : ", ret3);
        return [ty, ret3, ret4];
    }

    private static generateAddSub(arr:string[], ret:string[], totArr:string[]){
        let a = arr[0];
        let op = ret[1];
        let maxCommaNum:number = 0;
        for(let i=0; i<totArr.length; i++){            
            if(totArr[i].indexOf(".")>=0){
                maxCommaNum = Math.max(totArr[i].length - totArr[i].indexOf('.')-1, maxCommaNum);
            }
        }
        ret.push("_");

        let b:string = String(arr[1]);
        let c:string = this.calc(a, op, b);
        ret.push(c);
        for(let i=0; i<ret.length; i++){
            if(ret[i].match(/[0-9.]+/g)){
                let commaNum:number = 0;
                if(ret[i].indexOf('.')>=0){
                    commaNum = ret[i].length - ret[i].indexOf('.') - 1;                    
                }
                for(let idx=0; idx<maxCommaNum-commaNum; idx++){
                    ret[i] = ret[i]+"口";
                }
            }
        }
        return ret;
    }

    private static eqToTxtAddSub(eq0:string[]){
        let ret:string[] = [];
        ret.push(eq0[0].replace(/ /g, '口'));
        ret.push(eq0[1] + ' ' + eq0[2].replace(/ /g, '口'));
        let maxLen =  0;
        for(let s of eq0){
            maxLen = Math.max(s.length, maxLen);
        }
        let line = this.printLine(maxLen);
        ret.push(line);
        ret.push(eq0[4].replace(/ /g, '口'))
        return ret;
    }

    private static generateMult(arr:string[], ret:string[]){
        let a:string = arr[0];
        let a2:number = parseFloat(arr[0]); //第一个数
        while(Math.floor(a2) != a2){
            a2 *= 10;
        }
        let a3:string = String(Math.floor(a2));
        let op:string = ret[1];
        ret.push("_");
        if(arr[1].length > 1){
            for(let i=arr[1].length-1; i>=0; i--){
                let b:string = arr[1].charAt(i)
                if(b == '.'){
                    continue
                }
                if(b == '0'){
                    ret.push('!');
                    continue;
                }
                let c = this.calc(a3, op, b);                
                ret.push(c);
            }
        }else{
            let b:string = arr[1];
            let c:string = this.calc(a3, op, b);
            ret.push(c);
        }

        if(ret[ret.length-1] == '!' && ret.length == 6){
            ret.pop();
            let c:string = this.calc(a, op, arr[1]);
            ret[ret.length-1] = c;
        }
        else if(arr[1].length > 1){
            ret.push('_');
            let b:string = arr[1];
            let c:string = this.calc(a, op, b);
            ret.push(c);            
        }

        let dotNum:number = 0;
        for(let i=0; i<ret[0].length; i++){
            if(ret[0].charAt(i) == '.'){
                dotNum += ret[0].length - i;
            }
        }
        for(let i=0; i<ret[2].length ; i++){
            if(ret[2].charAt(i) == '.'){
                dotNum += ret[2].length - i;
            }
        }

        let tmpAns:string = ret[ret.length -1];
        if(tmpAns.length < dotNum && tmpAns.indexOf('.')<0){
            tmpAns += '.';
            dotNum ++;
        }
        while(tmpAns.length < dotNum){
            tmpAns += '0';
        }
        ret[ret.length-1] = tmpAns;
        //多位数乘法是否只有一个有效乘积
        let hasMoreNum:number = 0;
        for(let i=4; i<ret.length-2; i++){
            let s = ret[i];
            if(s!='!'){
                hasMoreNum ++ ;
                if(hasMoreNum >= 2){
                    break;
                }
            }
        }
        if(hasMoreNum == 1){
            let ret2:string[] = []
            ret2 = ret2.concat(ret.slice(0,4));
            ret2.push(ret[ret.length-1]);
            ret = ret2;
        }
        return ret;
    }

    private static eqToTxtMult(eq0:string[]){
        let ret:string[] = [];
        let maxLen:number = 0;
        for(let s of eq0){
            maxLen = Math.max(s.length, maxLen);            
        }
        let line:string = this.printLine(maxLen);
        ret.push(eq0[0]);
        ret.push(eq0[1] + ' ' + eq0[2]);
        ret.push(line);
        
        let space:string = '';
        for(let i=4; i<eq0.length; i++){
            let s = eq0[i];
            if(s == '_'){
                ret.push(line);
                continue;
            }
            if(s == '!'){
                space += "口";
                continue;
            }
            if(i == eq0.length-1){
                ret.push(s);
            }else{
                ret.push(s + space);
            }
            space += "口";        
        }
        return ret;
    }

    
    private static generateDivide(arr:string[], ret:string[]){
        let a:string = arr[0];
        let op:string = ret[1];
        let skipRemainder = '';
        let divisorStr = '';
        let finalQuotient = '';
        let dividend = parseFloat(arr[1]);
        // let dividendFloatValue = dividend - Math.floor(dividend)
        // console.log("dividendFloatValue : ",dividend , dividendFloatValue)
        if(dividend==0){
            return ret; 
        }
        let started:boolean = false;
        let tmpSkip:number = 0;
        for(let i=0; i<a.length; i++){
            if(divisorStr.length == 0 && a.charAt(i) == '0' && started){                
                // if(skipRemainder.length > 0){
                //     skipRemainder += '0';
                // }                
                skipRemainder += '0';
                finalQuotient += '0';                        
                if(i == a.length-1){
                    if(skipRemainder.length > 1){                        
                        ret.push(skipRemainder.replace(/^0+/, ''));                        
                    }
                    else{
                        ret.push(skipRemainder);
                    }
                    
                    ret.push(finalQuotient);
                    continue;
                }
                tmpSkip ++;
                continue;
            }            
            if(tmpSkip > 0){
                for(let sk=0;sk<tmpSkip; sk++){
                    ret.push('!');
                }
            }
            tmpSkip = 0;

            divisorStr+= a.charAt(i);
            if(a.charAt(i) == '.'){
                continue
            }
            
            let divisor = parseFloat(divisorStr);
            if(divisor < dividend && started){                
                skipRemainder += a.charAt(i);                
                finalQuotient += '0';
                if(i == a.length-1){                    
                    if(skipRemainder.length>1){
                        ret.push(skipRemainder.replace(/^0+/, ''));
                    }
                    else{
                        ret.push(skipRemainder);
                    }
                    // else{
                    //     ret.push('0');
                    // }
                    ret.push(finalQuotient);
                    continue;
                }
                ret.push('!');
                continue;
            // }else if(divisorFloatValue >= dividendFloatValue){
            }else if(divisor >= dividend){
                if(started){
                    ret.push(divisorStr);
                }
                let tmpstr:string = this.calc(divisorStr, op, arr[1]);
                let tmpresult:string[] = tmpstr.split('&&');
                let quotient:string = tmpresult[0];
                let remainder:string = tmpresult[1];                
                if(i==a.length-1){
                    ret.push(this.calc(quotient, '×', arr[1]));
                    ret.push("_");
                    if(remainder.length > 1){
                        ret.push(remainder.replace(/^0+/, ''));
                    }else{
                        ret.push(remainder);
                    }
                    
                    finalQuotient += quotient;                    
                    ret.push(finalQuotient);
                    break;
                }
                ret.push(this.calc(quotient, "×", arr[1]));
                ret.push('_');
                if(remainder == '0'){
                    divisorStr = ''
                }else{
                    divisorStr = remainder;
                }
                if(!started){
                    started = true;
                
                }
                finalQuotient += quotient;
            }
            
        }
        return ret;
    }

    private static generateDivide2(arr:string[], ret:string[]){
        let isInifinite:boolean = true;
        let divisorStr:string= arr[0];
        let dividendStr:string= arr[1];
        let divisor:number = parseFloat(divisorStr);
        let dividend:number = parseFloat(dividendStr);

        if(Math.floor(divisor)==divisor && Math.floor(dividend)==dividend){
            //整数被除数>除数，整数除法取余数
            if(divisor > dividend){
                console.log("整数除法")
                return this.generateDivide(arr,ret);
            }
        }
        console.log("小数除法")
        //整|小数被除数<除数，小数除法, 去除小数位得到整数除法的过程后替换被除数和除数
        let finalQuotient = parseFloat(PricisionCalc.div(divisor, dividend).toFixed(5));
        if(PricisionCalc.mul(dividend, finalQuotient) == divisor){            
            isInifinite = false;
        }
        if(isInifinite){
            console.log("无限小数除法")
        }else{
            console.log("有限小数除法")
        }
        
        let tmpFinalQuetient = finalQuotient;
        while(Math.floor(tmpFinalQuetient)!=tmpFinalQuetient){
            tmpFinalQuetient*=10;
        }        
        tmpFinalQuetient = Math.floor(tmpFinalQuetient);
        // console.log("tmpFinalQuetient : ", tmpFinalQuetient)

        let tmpDividend = dividend;
        while(Math.floor(tmpDividend)!=tmpDividend){
            tmpDividend *= 10;
        }
        tmpDividend = Math.floor(tmpDividend);
        // console.log("tmpDividend : ", tmpDividend);

        let tmpDivisor = PricisionCalc.mul(tmpDividend, tmpFinalQuetient);
        // console.log("tmpDivisor : ", tmpDivisor);
        let a:string = String(tmpDivisor);
        let b:string = String(tmpDividend);
        let tmpArr:string[] = [a,b];
        let tmpRet:string[] = [a, '÷', b];
        try{
            tmpRet = this.generateDivide(tmpArr, tmpRet);            
        }catch(err){
            console.log("generateDivide err : ", err);            
        }
        // console.log("tmpRet : ", tmpRet);
        let validNum:number = 0;
        for(let i=0;i<divisorStr.length; i++){
            let c:string = divisorStr.charAt(i);
            if(c == '0' || c == '.'){
                validNum ++;
            }else{
                break;
            }
        }

        let spaceToAdd:number = a.length - (divisorStr.length - validNum);
        if(isInifinite){
            spaceToAdd = 5;
        }
        for(let i=0; i<spaceToAdd; i++){
            divisorStr += ' ';
        }
        tmpRet[0] = divisorStr;
        tmpRet[2] = dividendStr;
        tmpRet[tmpRet.length-1] = String(finalQuotient);
        return tmpRet;
    }

    private static eqToTxtDivide(eq0:string[]){
        let divisor:string = eq0[0];
        let dividend:string = eq0[2];
        let quotient:string = eq0[eq0.length-1];
        let startPos:number = 0;
        let lenQuotient = quotient.length;
        if(quotient.indexOf('.') >=0 ){
            lenQuotient --;
        }
        for(let p=0; p<quotient.length; p++){
            let pstr:String = quotient.charAt(p);
            if(pstr == '0'){
                startPos ++;
            }else if(pstr == '.'){

            }else{
                break;
            }
        }

        let ret :string[] = [];
        ret.push(quotient);

        let line:string = this.printLine(divisor.length);
        ret.push(line);
        ret.push(dividend +"/" + divisor.replace(/ /g, '口'));

        let curQuo = 0;
        for(let i=3; i<eq0.length-1; i++){
            let s:string = eq0[i];
            if(s == '_'){
                ret.push(line);
                if(i!=eq0.length-3 || (i==eq0.length-3 && eq0[eq0.length-2]!='0' && curQuo<lenQuotient-1)){
                    curQuo ++;
                }
                continue;
            }
            if(s == '!'){
                curQuo ++ ;
                continue;
            }
            let spacetoadd:number = (lenQuotient-startPos) - curQuo - 1;
            for(let j=0; j<spacetoadd; j++){
                s+='口';
            }
            ret.push(s);
        }
        return ret;
    }

    private static calc(a0:string, op:string, b0:string){
        let c:number|string
        let a1:number = parseFloat(a0.replace("口",""));
        let b1:number = parseFloat(b0.replace("口",""));       
        switch(op){
            case '+':
                // c = a1+b1;
                c = PricisionCalc.add(a1,b1);
                break;
            case '-':
                // c = a1-b1;
                c = this.accSub(a1, b1);
                break;
            case '×':
                // c = a1*b1;
                c = PricisionCalc.mul(a1,b1);//this.accMul(a1, b1);
                break;
            case '÷':
                let c1 = Math.floor(a1/b1);
                let c2 = a1%b1;                
                return c1 + '&&' + c2;

        }
        return String(c);
    }

    private static accAdd(arg1,arg2){ 
        var r1,r2,m; 
        try{r1=arg1.toString().split(".")[1].length}catch(e){r1=0} 
        try{r2=arg2.toString().split(".")[1].length}catch(e){r2=0} 
        m=Math.pow(10,Math.max(r1,r2))         
        return (arg1*m+arg2*m)/m 
    } 

    private static accSub(arg1,arg2){ 
        var r1,r2,m,n; 
        try{r1=arg1.toString().split(".")[1].length}catch(e){r1=0} 
        try{r2=arg2.toString().split(".")[1].length}catch(e){r2=0} 
        m=Math.pow(10,Math.max(r1,r2)); 
        //动态控制精度长度 
        n=(r1>=r2)?r1:r2; 
        return ((arg1*m-arg2*m)/m).toFixed(n); 
    }

    private static accMul(arg1,arg2){ 
        var m=0,s1=arg1.toString(),s2=arg2.toString(); 
        try{m+=s1.split(".")[1].length}catch(e){} 
        try{m+=s2.split(".")[1].length}catch(e){} 
        return Number(s1.replace(".",""))*Number(s2.replace(".",""))/Math.pow(10,m) 
    } 

    private static printLine(len:number){
        let l:string = "";
        for(let i=0;i<len;i++){
            l+="_";
        }
        return l;
    }

}