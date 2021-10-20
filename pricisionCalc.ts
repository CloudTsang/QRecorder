class PricisionCalc{
    /**
     * 补0
     * @param {*} num 0个数
     */
    private static  padding0(num) {
        let str = ''
        while (num--) str += '0'
        return str
    }

    /**
     * 将科学记数法转为普通字符串
     * @param {Number} number
     */
    private static  noExponent(number) {
        const data = String(number).split(/[eE]/)
        if (data.length == 1) return data[0]

        let z = ''
        const sign = number < 0 ? '-' : ''
        const str = data[0].replace('.', '')
        let mag = Number(data[1]) + 1;

        if (mag < 0) {
            z = sign + '0.'
            while (mag++) z += '0'
            return z + str.replace(/^\-/, '')
        }
        mag -= str.length
        while (mag--) z += '0'
        return str + z
    }

    private static  split(number) {
        let str
        if (number < 1e-6) {
            str = this.noExponent(number)
        } else {
            str = number + ''
        }
        const index = str.lastIndexOf('.')
        if (index < 0) {
            return [str, 0]
        } else {
            return [str.replace('.', ''), str.length - index - 1]
        }
    }

    /**
     * 计算
     * @param {*} l 操作数1
     * @param {*} r 操作数2
     * @param {*} sign 操作符
     * @param {*} f 精度
     */
    private static  operate(l, r, sign, f) {
        switch (sign) {
            case '+': return (l + r) / f
            case '-': return (l - r) / f
            case '*': return (l * r) / (f * f)
            case '/': return (l / r)
        }
    }

    /**
     * 解决小数精度问题
     * @param {*} l 操作数1
     * @param {*} r 操作数2
     * @param {*} sign 操作符
     * fixedFloat(0.3, 0.2, '-')
     */
    private static  fixedFloat(l, r, sign) {
        const arrL = this.split(l)
        const arrR = this.split(r)
        let fLen = Math.max(arrL[1], arrR[1])

        if (fLen === 0) {
            return this.operate(Number(l), Number(r), sign, 1)
        }
        const f = Math.pow(10, fLen)
        if (arrL[1] !== arrR[1]) {
            if (arrL[1] > arrR[1]) {
                arrR[0] += this.padding0(arrL[1] - arrR[1])
            } else {
                arrL[0] += this.padding0(arrR[1] - arrL[1])
            }
        }
        return this.operate(Number(arrL[0]), Number(arrR[0]), sign, f)
    }

    /**
     * 加
     */
    public static  add(l, r) {
        return this.fixedFloat(l, r, '+')
    }

    /**
     * 减
     */
    public static  sub(l, r) {
        return this.fixedFloat(l, r, '-')
    }

    /**
     * 乘
     */
    public static  mul(l, r) {
        return this.fixedFloat(l, r, '*')
    }

    /**
     * 除
     */
    public static  div(l, r) {
        return this.fixedFloat(l, r, '/')
    }

    /**
     * 四舍五入
     * @param {*} number
     * @param {*} fraction
     */
    private static  round(number, fraction) {
        return Math.round(number * Math.pow(10, fraction)) / Math.pow(10, fraction)
    }
}