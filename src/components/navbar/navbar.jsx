import React from 'react'
import './navbar.css'
import  {RiMenu3Line,RiCloseLine} from 'react-icons/ri';

import logo from '../../../src/assets/images/400PngdpiLogo.png'
//BEM -> BLOCK ELEMENTS MODIFIER
const Navbar= () =>{
      return (
        <div className="gpt3__navbar">
            <div className="gpt3__navbar-links">
            <div className="gpt3__navbar-links_logo">
                <img src={logo} alt="logo" />
                />

            </div>
                <div className="gpt3__navbar-links_container">
                 <p><a href="#">Home</a></p>
                    <p><a href="/gpt3">Open AI</a></p>
                    <p><a href="/feature">Features</a></p>
                    <p><a href="/blog">Bolg</a></p>

                </div>


            </div>
            Navbar is a React component that allows you to create navigation
        </div>

    )

}

export default Navbar