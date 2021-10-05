import React, { useState } from 'react'

export default function ItemRow (props) {
    const [orderAmount, setOrderAmount] = useState(0)

    // modifies input field for Order Amount column
    // and calls parent component to change the 
    // amount for the given item in 'lowStockItems'
    function handleChange(event) {
        let val = parseInt(event.target.value)
        val = Number.isNaN(val) ? 0 : val
        setOrderAmount(val)
        props.changeOrderAmount(props.SKU, val)
    }

    return (
        <tr>
            <td>{props.SKU}</td>
            <td>{props.itemName}</td>
            <td>{props.amount}</td>
            <td>{props.capacity}</td>
            <td>
                <input type='text'
                    value={orderAmount}
                    onChange={handleChange}
                    size={'4'}
                >
                </input>
            </td>
        </tr>
    )
}
