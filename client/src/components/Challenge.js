import ItemRow from '../components/ItemRow'
import React, { useState } from 'react'
import Button from '@material-ui/core/Button'

export default function Challenge() {

  const [lowStockItems, setList] = useState([])
  const [restockPrice, setPrice] = useState(0)
  const buttonStyle = {
    backgroundColor: '#02a3e8',
    color: 'white',
    margin: '5px 0 0 5px',
    textTransform: 'none'
  }
  // for producing an unique key for every ItemRow
  let count = 0

  // fetch call to 'low-stock' API endpoint
  // retrieves and modifies list lowStockItems
  function fetchLowStockItems() {
    fetch('http://localhost:4567/low-stock')
      .then(response => response.json())
      .then(data => setList(data))

    count = 0
  }

  // helper function for fetchRestockPrice
  // returns JSON of items and their requested amounts
  function getOrderAmtsJSON() {
    let orderAmtsArray = []
    lowStockItems.map(item => {
      if (item.orderAmt > 0) {
        orderAmtsArray.push({ 'id': item.id, 'amount': item.orderAmt })
      }
      return item
    })
    return JSON.stringify(orderAmtsArray)
  }

  // fetch call to API endpoint 'restock-cost'
  // sends items and their requested amounts
  // retrieves lowest re-order cost
  function fetchRestockPrice() {
    fetch('http://localhost:4567/restock-cost', {
      method: 'POST',
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      },
      // helper function for sending data in JSON
      body: getOrderAmtsJSON()
    })
      .then(response => response.json())
      .then(data => setPrice(data.cost))
  }

  // modifies order amount of item in lowStockItems list
  // when 'Order Amount' input field is changed in child 
  // component ItemRow
  function changeOrderAmount(id, amount) {
    const newList = lowStockItems.map(item => {
      if (item.id === id) return { ...item, orderAmt: parseInt(amount, 10) }
      return item
    })
    setList(newList)

  }

  return (
    <div>
      <table>
        <thead>
          <tr>
            <td>SKU</td>
            <td>Item Name</td>
            <td>Amount in Stock</td>
            <td>Capacity</td>
            <td>Order Amount</td>
          </tr>
        </thead>
        <tbody>

          {lowStockItems.map(item => {
            count += 1
            return (
              // 'count' used for unique key for each
              // ItemRow child component
              <ItemRow key={count}
                SKU={item.id}
                itemName={item.name}
                amount={item.amount}
                capacity={item.capacity}
                orderAmount={item.orderAmt}
                changeOrderAmount={changeOrderAmount}
              >
              </ItemRow>
            )
          })

          }
        </tbody>
      </table>
      <br/>
      <div style={{marginLeft: '5px'}}>
        Total Cost: $ {lowStockItems.length > 0 ? (Math.round(restockPrice * 100) / 100).toFixed(2) : ''}
      </div>

      <Button variant='contained' style={buttonStyle} 
              onClick={fetchLowStockItems} disableElevation>
        Get Low-Stock Items
      </Button>
      <Button variant='contained' style={buttonStyle} 
              onClick={fetchRestockPrice} disableElevation>
        Determine Re-Order Cost
      </Button>
      
    </div>
  )
}
